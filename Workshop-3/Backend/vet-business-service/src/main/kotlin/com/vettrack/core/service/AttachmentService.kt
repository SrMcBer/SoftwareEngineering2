package com.vettrack.core.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.vettrack.core.domain.Attachment
import com.vettrack.core.domain.Exam
import com.vettrack.core.domain.Patient
import com.vettrack.core.domain.User
import com.vettrack.core.domain.Visit
import com.vettrack.core.repository.AttachmentRepository
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import com.vettrack.core.storage.LocalAttachmentStorage
import java.time.OffsetDateTime
import java.util.UUID

@Service
class AttachmentService(
    private val attachmentRepository: AttachmentRepository,
    private val patientService: PatientService,
    private val visitService: VisitService,
    private val examService: ExamService,
    private val userService: UserService,
    private val auditLogService: AuditLogService,
    private val localAttachmentStorage: LocalAttachmentStorage,
    private val objectMapper: ObjectMapper
) {
    // ------------------- Upload & Creation -------------------

    /**
     * Upload attachment with multipart file
     */
    fun uploadAttachmentMultipart(
        patientId: UUID,
        visitId: UUID? = null,
        examId: UUID? = null,
        type: String,
        file: MultipartFile,
        uploadedByUserId: UUID?,
        actorIp: String?
    ): Attachment {
        // Enforce basic constraint check early
        require(!(visitId != null && examId != null)) {
            "Attachment cannot be linked to both a visit and an exam"
        }

        // 1. Lookups
        val patient: Patient = patientService.getById(patientId)
        val visit: Visit? = visitId?.let { uid -> visitService.getById(uid) }
        val exam: Exam? = examId?.let { uid -> examService.getExamById(uid) }
        val uploadedBy: User? = uploadedByUserId?.let { uid -> userService.getById(uid) }

        // 2. Validate file
        if (!isValidType(type, file.contentType)) {
            throw IllegalArgumentException("Invalid file type: $type vs ${file.contentType}")
        }
        if (file.isEmpty) {
            throw IllegalArgumentException("File cannot be empty")
        }

        // 3. Create entity first to get ID for storage path
        val now = OffsetDateTime.now()
        val attachment = Attachment(
            patient = patient,
            visit = visit,
            exam = exam,
            type = type,
            url = "", // Placeholder, will be updated after storage
            filename = file.originalFilename,
            uploadedBy = uploadedBy,
            uploadedAt = now
        )

        val saved = attachmentRepository.save(attachment)

        // 4. Store file using the generated attachment ID
        try {
            val storedFile = localAttachmentStorage.save(
                patientId = patientId,
                attachmentId = saved.id!!,
                file = file
            )

            // Update with actual file key
            saved.url = storedFile.fileKey
            val updated = attachmentRepository.save(saved)

            // 5. Audit Logging
            val diffJson = buildDiffJson(old = null, new = updated, action = "upload")
            auditLogService.log(
                actorUserId = uploadedByUserId,
                entityType = "attachment",
                entityId = updated.id!!,
                action = "upload",
                diffSnapshotJson = diffJson,
                ip = actorIp
            )

            return updated
        } catch (e: Exception) {
            // Rollback: delete the database record if file storage fails
            attachmentRepository.delete(saved)
            throw IllegalStateException("Failed to store file: ${e.message}", e)
        }
    }

    // ------------------- Deletion (Manage File Lifecycle) -------------------

    fun deleteAttachment(
        attachmentId: UUID,
        actorUserId: UUID?,
        actorIp: String?
    ) {
        val attachment = attachmentRepository.findById(attachmentId).orElseThrow {
            NoSuchElementException("Attachment $attachmentId not found")
        }

        // Delete from storage first
        try {
            localAttachmentStorage.delete(attachment.url)
        } catch (e: Exception) {
            // Log but continue - orphaned files can be cleaned up later
            println("WARN: Failed to delete file ${attachment.url}: ${e.message}")
        }

        attachmentRepository.delete(attachment)

        // Audit Logging for Deletion
        val diffJson = buildDiffJson(old = attachment, new = null, action = "delete")
        auditLogService.log(
            actorUserId = actorUserId,
            entityType = "attachment",
            entityId = attachmentId,
            action = "delete",
            diffSnapshotJson = diffJson,
            ip = actorIp
        )
    }

    // ------------------- Download -------------------

    fun getAttachment(attachmentId: UUID): Attachment {
        return attachmentRepository.findById(attachmentId).orElseThrow {
            NoSuchElementException("Attachment $attachmentId not found")
        }
    }

    // ------------------- Listing -------------------

    fun listForPatient(patientId: UUID): List<Attachment> =
        attachmentRepository.findByPatientId(patientId)

    fun listForVisit(visitId: UUID): List<Attachment> =
        attachmentRepository.findByVisitId(visitId)

    fun listForExam(examId: UUID): List<Attachment> =
        attachmentRepository.findByExamId(examId)

    // ------------------- Validation -------------------

    private fun isValidType(declaredType: String, contentType: String?): Boolean {
        val allowedTypes = mapOf(
            "image" to listOf("image/jpeg", "image/png", "image/gif", "image/webp"),
            "pdf" to listOf("application/pdf","application/json"),
            "video" to listOf("video/mp4", "video/quicktime", "video/x-msvideo")
        )

        val allowed = allowedTypes[declaredType] ?: return false
        return contentType in allowed
    }

    // ------------------- Audit Diff Helper -------------------

    private fun buildDiffJson(
        old: Attachment?,
        new: Attachment?,
        action: String
    ): String? {
        val diff = mutableMapOf<String, Any?>()
        diff["action"] = action

        val snapshot = new ?: old ?: return null

        diff[if (new != null) "new" else "old"] = mapOf(
            "id" to snapshot.id,
            "patientId" to snapshot.patient.id,
            "linkedTo" to when {
                snapshot.visit != null -> "visit:${snapshot.visit?.id}"
                snapshot.exam != null -> "exam:${snapshot.exam?.id}"
                else -> null
            },
            "filename" to snapshot.filename,
            "type" to snapshot.type,
            "fileKey" to snapshot.url
        )
        return objectMapper.writeValueAsString(diff)
    }
}