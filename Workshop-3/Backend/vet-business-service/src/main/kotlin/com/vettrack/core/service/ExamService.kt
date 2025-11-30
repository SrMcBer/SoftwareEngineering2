package com.vettrack.core.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.vettrack.core.domain.*
import com.vettrack.core.repository.ExamRepository
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.UUID

@Service
class ExamService(
    private val examRepository: ExamRepository,
    private val examTemplateService: ExamTemplateService,
    private val patientService: PatientService,
    private val visitService: VisitService,
    private val userService: UserService,
    private val auditLogService: AuditLogService,
    private val objectMapper: ObjectMapper
) {

    // ------------------- Creation -------------------

    fun createExamFromTemplate(
        patientId: UUID,
        visitId: UUID,
        templateId: UUID,
        performedByUserId: UUID?,
        vitalsJson: String? = null,
        resultsJson: String,
        status: ExamStatus = ExamStatus.DRAFT,
        actorUserId: UUID?, // Audit actor
        actorIp: String?    // Audit IP
    ): Exam {
        val patient = patientService.getById(patientId)
        val visit = visitService.getById(visitId)

        require(visit.patient.id == patient.id) {
            "Visit $visitId does not belong to patient $patientId"
        }

        val template = examTemplateService.getById(templateId)
        val performedBy: User? = performedByUserId?.let { uid -> userService.getById(uid) }

        val now = OffsetDateTime.now()
        val exam = Exam(
            patient = patient,
            visit = visit,
            template = template,
            templateVersion = template.version,
            performedAt = now,
            performedBy = performedBy,
            vitalsJson = vitalsJson,
            resultsJson = resultsJson,
            status = status,
            createdAt = now,
            updatedAt = now,
        )

        val saved = examRepository.save(exam)

        // --- Audit Logging ---
        val diffJson = buildDiffJson(old = null, new = saved, action = "create")
        auditLogService.log(
            actorUserId = actorUserId,
            entityType = "exam",
            entityId = saved.id!!,
            action = "create",
            diffSnapshotJson = diffJson,
            ip = actorIp
        )

        return saved
    }

    // ------------------- Update -------------------

    fun updateExam(
        examId: UUID,
        vitalsJson: String? = null,
        resultsJson: String? = null,
        actorUserId: UUID?, // Audit actor
        actorIp: String?    // Audit IP
    ): Exam {
        val existing = examRepository.findById(examId).orElseThrow {
            NoSuchElementException("Exam $examId not found")
        }

        // Only allow editing if the exam is in DRAFT status
        require(existing.status == ExamStatus.DRAFT) {
            "Cannot update exam $examId: only DRAFT exams can be modified."
        }

        val before = existing.shallowCopy()

        vitalsJson?.let { existing.vitalsJson = it }
        resultsJson?.let { existing.resultsJson = it }
        existing.updatedAt = OffsetDateTime.now()

        val saved = examRepository.save(existing)

        // --- Audit Logging ---
        val diffJson = buildDiffJson(old = before, new = saved, action = "update")
        auditLogService.log(
            actorUserId = actorUserId,
            entityType = "exam",
            entityId = saved.id!!,
            action = "update",
            diffSnapshotJson = diffJson,
            ip = actorIp
        )

        return saved
    }

    // ------------------- Finalize -------------------

    fun finalizeExam(
        examId: UUID,
        actorUserId: UUID?, // Audit actor
        actorIp: String?    // Audit IP
    ): Exam {
        val exam = examRepository.findById(examId).orElseThrow {
            NoSuchElementException("Exam $examId not found")
        }

        // Prevent audit if already finalized
        if (exam.status == ExamStatus.FINAL) return exam

        val before = exam.shallowCopy()
        exam.finish() // Assume this sets status to FINAL and updatedAt
        val saved = examRepository.save(exam)

        // --- Audit Logging ---
        val diffJson = buildDiffJson(old = before, new = saved, action = "finalize")
        auditLogService.log(
            actorUserId = actorUserId,
            entityType = "exam",
            entityId = saved.id!!,
            action = "finalize",
            diffSnapshotJson = diffJson,
            ip = actorIp
        )

        return saved
    }

    // ------------------- Read/Search -------------------

    fun getExamById(examId: UUID): Exam =
        examRepository.findById(examId).orElseThrow {
            NoSuchElementException("Exam $examId not found")
        }

    fun listForPatient(patientId: UUID): List<Exam> =
        examRepository.findByPatientIdOrderByPerformedAtDesc(patientId)

    // ------------------- Diff builder for audit -------------------

    private fun buildDiffJson(
        old: Exam?,
        new: Exam?,
        action: String
    ): String? {
        val diff = mutableMapOf<String, Any?>()
        diff["action"] = action

        if (old == null && new != null) {
            // Create: log key snapshot fields
            diff["new"] = mapOf("id" to new.id, "patient" to new.patient.id, "template" to new.template.id, "status" to new.status)
        } else if (old != null && new == null) {
            // Delete (if implemented): log key snapshot fields
            diff["old"] = mapOf("id" to old.id, "patient" to old.patient.id, "template" to old.template.id, "status" to old.status)
        } else if (old != null && new != null) {
            val changes = mutableMapOf<String, Any?>()

            // Compare mutable fields
            if (old.vitalsJson != new.vitalsJson) {
                changes["vitalsJson"] = mapOf("old" to old.vitalsJson, "new" to new.vitalsJson)
            }
            if (old.resultsJson != new.resultsJson) {
                changes["resultsJson"] = mapOf("old" to old.resultsJson, "new" to new.resultsJson)
            }
            if (old.status != new.status) {
                changes["status"] = mapOf("old" to old.status, "new" to new.status)
            }

            if (changes.isNotEmpty()) {
                diff["changes"] = changes
            }
        }

        return if (diff.size == 1 && diff.containsKey("action")) null else objectMapper.writeValueAsString(diff)
    }
}
