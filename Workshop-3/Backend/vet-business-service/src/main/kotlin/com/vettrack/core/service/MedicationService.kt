package com.vettrack.core.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.vettrack.core.domain.DoseEvent
import com.vettrack.core.domain.Medication
import com.vettrack.core.domain.Patient
import com.vettrack.core.domain.User
import com.vettrack.core.repository.DoseEventRepository
import com.vettrack.core.repository.MedicationRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@Service
class MedicationService(
    private val medicationRepository: MedicationRepository,
    private val doseEventRepository: DoseEventRepository,
    private val patientService: PatientService,
    private val userService: UserService,
    private val auditLogService: AuditLogService,
    private val objectMapper: ObjectMapper
) {

    fun prescribeMedication(
        patientId: UUID,
        name: String,
        dosage: String?,
        route: String?,
        frequency: String?,
        startDate: LocalDate?,
        endDate: LocalDate?,
        createdByUserId: UUID?
    ): Medication {
        val patient: Patient = patientService.getById(patientId)

        val createdBy: User? = createdByUserId?.let { uid -> userService.getById(uid) }

        val now = OffsetDateTime.now()
        val medication = Medication(
            patient = patient,
            name = name,
            dosage = dosage,
            route = route,
            frequency = frequency,
            startDate = startDate,
            endDate = endDate,
            createdBy = createdBy,
            createdAt = now,
            updatedAt = now
        )

        return medicationRepository.save(medication)
    }
// ------------------- Prescribe (CRUD) -------------------

    fun prescribeMedication(
        patientId: UUID,
        name: String,
        dosage: String?,
        route: String?,
        frequency: String?,
        startDate: LocalDate?,
        endDate: LocalDate?,
        createdByUserId: UUID?,
        actorUserId: UUID?, // Audit actor
        actorIp: String?    // Audit IP
    ): Medication {
        val patient: Patient = patientService.getById(patientId)

        val createdBy: User? = createdByUserId?.let { uid -> userService.getById(uid) }

        val now = OffsetDateTime.now()
        val medication = Medication(
            patient = patient,
            name = name,
            dosage = dosage,
            route = route,
            frequency = frequency,
            startDate = startDate,
            endDate = endDate,
            createdBy = createdBy,
            createdAt = now,
            updatedAt = now
        )

        val saved = medicationRepository.save(medication)

        // --- Audit Logging ---
        val diffJson = buildDiffJson(old = null, new = saved, action = "prescribe")
        auditLogService.log(
            actorUserId = actorUserId,
            entityType = "medication",
            entityId = saved.id!!,
            action = "prescribe",
            diffSnapshotJson = diffJson,
            ip = actorIp
        )

        return saved
    }

    // ------------------- Update Prescribing Details (Responsibility: update meds) -------------------

    fun updateMedication(
        medicationId: UUID,
        dosage: String?,
        route: String?,
        frequency: String?,
        startDate: LocalDate?,
        endDate: LocalDate?,
        actorUserId: UUID?,
        actorIp: String?
    ): Medication {
        val existing = medicationRepository.findById(medicationId).orElseThrow {
            NoSuchElementException("Medication $medicationId not found")
        }

        val before = existing.shallowCopy()

        dosage?.let { existing.dosage = it }
        route?.let { existing.route = it }
        frequency?.let { existing.frequency = it }
        startDate?.let { existing.startDate = it }
        endDate?.let { existing.endDate = it }

        existing.updatedAt = OffsetDateTime.now()

        // Recalculate next dose if frequency or dates changed
        if (before.frequency != existing.frequency || before.endDate != existing.endDate) {
            existing.nextDueAt = calculateNextDoseDueAt(existing)
        }

        val saved = medicationRepository.save(existing)

        // --- Audit Logging ---
        val diffJson = buildDiffJson(old = before, new = saved, action = "update_prescription")
        auditLogService.log(
            actorUserId = actorUserId,
            entityType = "medication",
            entityId = saved.id!!,
            action = "update_prescription",
            diffSnapshotJson = diffJson,
            ip = actorIp
        )

        return saved
    }

    fun endMedication(
        medicationId: UUID,
        endDate: LocalDate = LocalDate.now(),
        actorUserId: UUID?,
        actorIp: String?
    ): Medication {
        val med = medicationRepository.findById(medicationId).orElseThrow {
            NoSuchElementException("Medication $medicationId not found")
        }

        // Prevent audit if already ended on the same date
        if (med.endDate == endDate) return med

        val before = med.shallowCopy()
        med.endDate = endDate
        med.nextDueAt = null // Clear next due when medication ends
        med.updatedAt = OffsetDateTime.now()

        val saved = medicationRepository.save(med)

        // --- Audit Logging ---
        val diffJson = buildDiffJson(old = before, new = saved, action = "end_medication")
        auditLogService.log(
            actorUserId = actorUserId,
            entityType = "medication",
            entityId = saved.id!!,
            action = "end_medication",
            diffSnapshotJson = diffJson,
            ip = actorIp
        )
        return saved
    }

    // ------------------- Record Dose (Responsibility: Record DoseEvent) -------------------

    fun recordDose(
        medicationId: UUID,
        amount: String?,
        notes: String?,
        recordedByUserId: UUID?,
        actorIp: String? // Dose events are only created, so only need IP for the event itself
    ): DoseEvent {
        val med = medicationRepository.findById(medicationId).orElseThrow {
            NoSuchElementException("Medication $medicationId not found")
        }

        val recordedBy: User? = recordedByUserId?.let { uid -> userService.getById(uid) }

        val now = OffsetDateTime.now()
        val doseEvent = DoseEvent(
            medication = med,
            administeredAt = now,
            amount = amount,
            notes = notes,
            recordedBy = recordedBy
        )

        // Update Medication last/next due times
        val before = med.shallowCopy()
        med.lastAdministeredAt = now
        med.nextDueAt = calculateNextDoseDueAt(med) // RESPONSIBILITY: compute next dose
        med.updatedAt = now

        val savedMed = medicationRepository.save(med)
        val savedDose = doseEventRepository.save(doseEvent)

        // --- Audit Logging for Medication Update ---
        val diffJson = buildDiffJson(old = before, new = savedMed, action = "dose_recorded")
        auditLogService.log(
            actorUserId = recordedByUserId,
            entityType = "medication",
            entityId = savedMed.id!!,
            action = "dose_recorded",
            diffSnapshotJson = diffJson,
            ip = actorIp
        )
        // DoseEvent auditing is often omitted or handled separately, but we include it here
        // as a simple entry since it is a record of fact.
        auditLogService.log(
            actorUserId = recordedByUserId,
            entityType = "dose_event",
            entityId = savedDose.id!!,
            action = "create",
            diffSnapshotJson = objectMapper.writeValueAsString(mapOf("amount" to amount)),
            ip = actorIp
        )

        return savedDose
    }

    // ------------------- Read/Search -------------------

    fun listActiveForPatient(patientId: UUID): List<Medication> =
        medicationRepository.findByPatientIdAndEndDateIsNull(patientId)

    // ------------------- Helper Methods -------------------

    /**
     * RESPONSIBILITY: compute next dose.
     * Placeholder function: Actual implementation requires complex scheduling logic
     * based on the 'frequency' string (e.g., "BID", "q12h").
     */
    private fun calculateNextDoseDueAt(medication: Medication): OffsetDateTime? {
        if (medication.endDate != null && medication.endDate!!.isBefore(LocalDate.now())) {
            return null // Medication is already over
        }

        val lastTime = medication.lastAdministeredAt ?: medication.createdAt

        return when (medication.frequency?.uppercase()) {
            "BID" -> lastTime.plusHours(12)
            "SID" -> lastTime.plusHours(24)
            "TID" -> lastTime.plusHours(8)
            else -> null // Unknown frequency, cannot compute
        }
    }

    private fun buildDiffJson(
        old: Medication?,
        new: Medication?,
        action: String
    ): String? {
        val diff = mutableMapOf<String, Any?>()
        diff["action"] = action

        if (old != null && new != null) {
            val changes = mutableMapOf<String, Any?>()

            if (old.dosage != new.dosage) changes["dosage"] = mapOf("old" to old.dosage, "new" to new.dosage)
            if (old.route != new.route) changes["route"] = mapOf("old" to old.route, "new" to new.route)
            if (old.frequency != new.frequency) changes["frequency"] = mapOf("old" to old.frequency, "new" to new.frequency)
            if (old.endDate != new.endDate) changes["endDate"] = mapOf("old" to old.endDate, "new" to new.endDate)
            if (old.nextDueAt != new.nextDueAt) changes["nextDueAt"] = mapOf("old" to old.nextDueAt, "new" to new.nextDueAt)
            // lastAdministeredAt usually changes frequently and might be noisy to log unless explicitly needed

            if (changes.isNotEmpty()) diff["changes"] = changes
        }

        return if (diff.size == 1 && diff.containsKey("action")) null else objectMapper.writeValueAsString(diff)
    }
}
