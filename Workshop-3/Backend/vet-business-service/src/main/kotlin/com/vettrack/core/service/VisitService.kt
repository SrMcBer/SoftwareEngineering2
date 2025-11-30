package com.vettrack.core.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.vettrack.core.domain.Patient
import com.vettrack.core.domain.User
import com.vettrack.core.domain.Visit
import com.vettrack.core.repository.VisitRepository
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.UUID

@Service
class VisitService(
    private val visitRepository: VisitRepository,
    private val patientService: PatientService,
    private val userService: UserService,
    private val auditLogService: AuditLogService,
    private val objectMapper: ObjectMapper
) {
// ------------------- Creation -------------------

    fun createVisit(
        patientId: UUID,
        reason: String?,
        vitalsJson: String?,
        examNotes: String?,
        diagnoses: String?,
        procedures: String?,
        recommendations: String?,
        createdByUserId: UUID?,
        actorUserId: UUID?, // Audit actor
        actorIp: String?    // Audit IP
    ): Visit {
        // RESPONSIBILITY: Ensure patient exists before creating visit
        val patient: Patient = patientService.getById(patientId)

        val createdBy: User? = createdByUserId?.let { uid -> userService.getById(uid) }

        // RESPONSIBILITY: validate vitals (Placeholder)
        validateVitals(vitalsJson)

        val now = OffsetDateTime.now()
        val visit = Visit(
            patient = patient,
            dateTime = now,
            reason = reason,
            vitalsJson = vitalsJson,
            examNotes = examNotes,
            diagnoses = diagnoses,
            procedures = procedures,
            recommendations = recommendations,
            createdBy = createdBy,
            createdAt = now,
            updatedAt = now
        )

        val saved = visitRepository.save(visit)

        // --- Audit Logging ---
        val diffJson = buildDiffJson(old = null, new = saved, action = "create")
        auditLogService.log(
            actorUserId = actorUserId,
            entityType = "visit",
            entityId = saved.id!!,
            action = "create",
            diffSnapshotJson = diffJson,
            ip = actorIp
        )

        return saved
    }

    // ------------------- Update (CRUD) -------------------

    fun updateVisit(
        visitId: UUID,
        reason: String? = null,
        vitalsJson: String? = null,
        examNotes: String? = null,
        diagnoses: String? = null,
        procedures: String? = null,
        recommendations: String? = null,
        actorUserId: UUID?, // Audit actor
        actorIp: String?    // Audit IP
    ): Visit {
        val existing = getById(visitId)

        // Capture state before modification for auditing
        val before = existing.shallowCopy()

        // RESPONSIBILITY: validate vitals
        if (vitalsJson != null) {
            validateVitals(vitalsJson)
            existing.vitalsJson = vitalsJson
        }

        reason?.let { existing.reason = it }
        examNotes?.let { existing.examNotes = it }
        diagnoses?.let { existing.diagnoses = it }
        procedures?.let { existing.procedures = it }
        recommendations?.let { existing.recommendations = it }
        existing.updatedAt = OffsetDateTime.now()

        val saved = visitRepository.save(existing)

        // --- Audit Logging ---
        val diffJson = buildDiffJson(old = before, new = saved, action = "update")
        auditLogService.log(
            actorUserId = actorUserId,
            entityType = "visit",
            entityId = saved.id!!,
            action = "update",
            diffSnapshotJson = diffJson,
            ip = actorIp
        )

        return saved
    }

    // ------------------- Read/Search -------------------

    fun getById(id: UUID): Visit =
        visitRepository.findById(id).orElseThrow {
            NoSuchElementException("Visit $id not found")
        }

    fun listForPatient(patientId: UUID): List<Visit> =
        visitRepository.findByPatientIdOrderByDateTimeDesc(patientId)

    fun lastVisitForPatient(patientId: UUID): Visit? =
        visitRepository.findFirstByPatientIdOrderByDateTimeDesc(patientId)

    fun listBetween(start: OffsetDateTime, end: OffsetDateTime): List<Visit> =
        visitRepository.findByDateTimeBetween(start, end)

    // ------------------- Helper Methods -------------------

    /**
     * RESPONSIBILITY: validate vitals
     * Placeholder for logic to ensure vital signs JSON is valid.
     */
    private fun validateVitals(vitalsJson: String?) {
        if (vitalsJson.isNullOrBlank()) return

        // Here you would deserialize the JSON and check for required fields
        // (e.g., heart_rate, temperature) and value ranges.
        try {
            objectMapper.readTree(vitalsJson)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid vitalsJson format: ${e.message}")
        }
    }

    private fun buildDiffJson(
        old: Visit?,
        new: Visit?,
        action: String
    ): String? {
        val diff = mutableMapOf<String, Any?>()
        diff["action"] = action

        if (old != null && new != null) {
            val changes = mutableMapOf<String, Any?>()

            if (old.reason != new.reason) changes["reason"] = mapOf("old" to old.reason, "new" to new.reason)
            if (old.vitalsJson != new.vitalsJson) changes["vitalsJson"] = mapOf("old" to old.vitalsJson, "new" to new.vitalsJson)
            if (old.diagnoses != new.diagnoses) changes["diagnoses"] = mapOf("old" to old.diagnoses, "new" to new.diagnoses)
            if (old.procedures != new.procedures) changes["procedures"] = mapOf("old" to old.procedures, "new" to new.procedures)
            if (old.recommendations != new.recommendations) changes["recommendations"] = mapOf("old" to old.recommendations, "new" to new.recommendations)

            if (changes.isNotEmpty()) diff["changes"] = changes
        }

        return if (diff.size == 1 && diff.containsKey("action")) null else objectMapper.writeValueAsString(diff)
    }
}
