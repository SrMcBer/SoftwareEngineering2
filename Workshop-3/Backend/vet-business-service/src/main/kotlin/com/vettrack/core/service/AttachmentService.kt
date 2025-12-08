package com.vettrack.core.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.vettrack.core.domain.Attachment
import com.vettrack.core.domain.Exam
import com.vettrack.core.domain.Patient
import com.vettrack.core.domain.User
import com.vettrack.core.domain.Visit
import com.vettrack.core.repository.AttachmentRepository
import org.springframework.stereotype.Service
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
    private val objectMapper: ObjectMapper
) {
    // ------------------- Upload & Creation -------------------

    /**
     * Consolidates attachment creation to a visit OR an exam.
     * Enforces "visit XOR exam" link constraint.
     */
    fun uploadAttachment(
        patientId: UUID,
        visitId: UUID? = null,
        examId: UUID? = null,
        type: String,
        fileKey: String, // Use a generic key/path instead of a final URL here
        filename: String?,
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

        // 2. Pre-save Responsibilities
        if (!isSafeAndValidType(fileKey, type)) {
            throw IllegalArgumentException("File failed virus or type validation.")
        }
        // The final, persistent URL is managed/created here after successful S3/storage upload.
        val permanentUrl = generatePermanentUrl(fileKey)

        // 3. Entity Creation
        val now = OffsetDateTime.now()
        val attachment = Attachment(
            patient = patient,
            visit = visit,
            exam = exam,
            type = type,
            url = permanentUrl,
            filename = filename,
            uploadedBy = uploadedBy,
            uploadedAt = now
        )

        val saved = attachmentRepository.save(attachment)

        // 4. Audit Logging (Only Creation is logged for this entity)
        val diffJson = buildDiffJson(old = null, new = saved, action = "upload")
        auditLogService.log(
            actorUserId = uploadedByUserId,
            entityType = "attachment",
            entityId = saved.id!!,
            action = "upload",
            diffSnapshotJson = diffJson,
            ip = actorIp
        )

        return saved
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

        // RESPONSIBILITY: manage file lifecycle (Delete from cloud storage first)
        deleteFileFromStorage(attachment.url)

        val patientId = attachment.patient.id!! // Capture ID before deletion
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

    // ------------------- Download (Signed URLs) -------------------

    /**
     * RESPONSIBILITY: Upload/download via signed URLs
     * Generates a time-limited URL for secure access.
     */
    fun getSignedDownloadUrl(attachmentId: UUID): String {
        val attachment = attachmentRepository.findById(attachmentId).orElseThrow {
            NoSuchElementException("Attachment $attachmentId not found")
        }
        // TODO Placeholder for secure cloud storage client call
        //
        return generateSignedUrl(attachment.url)
    }

    // ------------------- Listing -------------------

    fun listForPatient(patientId: UUID): List<Attachment> =
        attachmentRepository.findByPatientId(patientId)

    fun listForVisit(visitId: UUID): List<Attachment> =
        attachmentRepository.findByVisitId(visitId)

    fun listForExam(examId: UUID): List<Attachment> =
        attachmentRepository.findByExamId(examId)

    // ------------------- External/Placeholder Logic -------------------

    /** Placeholder for integration with external storage (S3, Azure Blob, etc.) */
    private fun isSafeAndValidType(fileKey: String, fileType: String): Boolean {
        // TODO
        // 1. Check fileType against allowed list (e.g., "image/jpeg", "application/pdf")
        // 2. Scan fileKey in storage for viruses/malware
        return true // Assume passed
    }

    private fun generatePermanentUrl(fileKey: String): String {
        // TODO
        // Converts a temporary file key into a permanent public/internal access URL
        return "https://cloudstorage.vettrack.com/attachments/$fileKey"
    }

    private fun generateSignedUrl(permanentUrl: String): String {
        // TODO
        // Generates a time-limited URL for secure download
        return permanentUrl + "?Expires=1678886400&Signature=XYZ..."
    }

    private fun deleteFileFromStorage(url: String) {
        // TODO
        // Sends request to cloud storage API to remove the file linked to the URL
        println("INFO: Deleting file from cloud storage: $url")
    }

    // ------------------- Audit Diff Helper -------------------

    private fun buildDiffJson(
        old: Attachment?,
        new: Attachment?,
        action: String
    ): String? {
        val diff = mutableMapOf<String, Any?>()
        diff["action"] = action

        // Attachment is highly immutable (only created/deleted), so full snapshot is sufficient.
        val snapshot = new ?: old ?: return null

        diff[if (new != null) "new" else "old"] = mapOf(
            "id" to snapshot.id,
            "patientId" to snapshot.patient.id,
            "linkedTo" to (if (snapshot.visit != null) "visit" else "exam"),
            "filename" to snapshot.filename,
            "type" to snapshot.type
        )
        return objectMapper.writeValueAsString(diff)
    }
}
