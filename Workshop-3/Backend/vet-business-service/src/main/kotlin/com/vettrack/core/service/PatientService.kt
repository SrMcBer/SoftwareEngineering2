package com.vettrack.core.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.vettrack.core.domain.Owner
import com.vettrack.core.domain.Patient
import com.vettrack.core.repository.OwnerRepository
import com.vettrack.core.repository.PatientRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@Service
class PatientService(
    private val patientRepository: PatientRepository,
    private val ownerRepository: OwnerRepository,
    private val auditLogService: AuditLogService,
    private val objectMapper: ObjectMapper
) {

    // ------------------- CRUD Methods -------------------

    fun registerPatient(
        ownerId: UUID,
        name: String,
        species: String,
        breed: String? = null,
        sex: String? = null,
        dob: LocalDate? = null,
        color: String? = null,
        microchipId: String? = null,
        allergies: String? = null,
        notes: String? = null,
        actorUserId: UUID?, // For auditing
        actorIp: String?    // For auditing
    ): Patient {
        val owner: Owner = ownerRepository.findById(ownerId).orElseThrow {
            NoSuchElementException("Owner $ownerId not found")
        }

        val now = OffsetDateTime.now()
        val patient = Patient(
            owner = owner,
            name = name,
            species = species,
            breed = breed,
            sex = sex,
            dob = dob,
            color = color,
            microchipId = microchipId,
            allergies = allergies,
            notes = notes,
            createdAt = now,
            updatedAt = now
        )

        val saved = patientRepository.save(patient)

        // --- Audit Logging ---
        val diffJson = buildDiffJson(old = null, new = saved, action = "create")
        auditLogService.log(
            actorUserId = actorUserId,
            entityType = "patient",
            entityId = saved.id!!,
            action = "create",
            diffSnapshotJson = diffJson,
            ip = actorIp
        )

        return saved
    }

    fun updatePatient(
        id: UUID,
        name: String? = null,
        species: String? = null,
        breed: String? = null,
        sex: String? = null,
        dob: LocalDate? = null,
        color: String? = null,
        microchipId: String? = null,
        allergies: String? = null,
        notes: String? = null,
        actorUserId: UUID?, // For auditing
        actorIp: String?    // For auditing
    ): Patient {
        val existing = patientRepository.findById(id).orElseThrow {
            NoSuchElementException("Patient $id not found")
        }

        // Capture state before modification for auditing
        val before = existing.shallowCopy()

        name?.let { existing.name = it }
        species?.let { existing.species = it }
        breed?.let { existing.breed = it }
        sex?.let { existing.sex = it }
        dob?.let { existing.dob = it }
        color?.let { existing.color = it }
        microchipId?.let { existing.microchipId = it }
        allergies?.let { existing.allergies = it }
        notes?.let { existing.notes = it }
        existing.updatedAt = OffsetDateTime.now()

        val saved = patientRepository.save(existing)

        // --- Audit Logging ---
        val diffJson = buildDiffJson(old = before, new = saved, action = "update")
        auditLogService.log(
            actorUserId = actorUserId,
            entityType = "patient",
            entityId = saved.id!!,
            action = "update",
            diffSnapshotJson = diffJson,
            ip = actorIp
        )

        return saved
    }

    fun deletePatient(
        id: UUID,
        actorUserId: UUID?, // For auditing
        actorIp: String?    // For auditing
    ) {
        val patient = patientRepository.findById(id).orElseThrow {
            NoSuchElementException("Patient $id not found")
        }

        // Optional check: Ensure no related records (visits, etc.) exist before hard delete
        // If related records exist, a soft delete (marking patient as inactive) might be preferred.

        patientRepository.delete(patient)

        // --- Audit Logging ---
        val diffJson = buildDiffJson(old = patient, new = null, action = "delete")
        auditLogService.log(
            actorUserId = actorUserId,
            entityType = "patient",
            entityId = id,
            action = "delete",
            diffSnapshotJson = diffJson,
            ip = actorIp
        )
    }

    // ------------------- Read/Search Methods -------------------

    fun getById(id: UUID): Patient =
        patientRepository.findById(id).orElseThrow {
            NoSuchElementException("Patient $id not found")
        }

    fun getByOwner(ownerId: UUID): List<Patient> =
        patientRepository.findByOwnerId(ownerId)

    fun searchByName(query: String): List<Patient> =
        patientRepository.findByNameContainingIgnoreCase(query)

    fun findByMicrochip(microchipId: String): Patient? =
        patientRepository.findByMicrochipId(microchipId)

    // ------------------- Aggregation/Coordination Responsibility -------------------

    /**
     * TODO:
     * Responsibility: Patient summary aggregation
     * Fetches core patient data and aggregates related counts/flags for a summary view.
     */
//    fun aggregatePatientSummary(id: UUID): PatientSummary {
//        val patient = getById(id)
//
//        val visitCount = visitRepository.countByPatientId(id)
//        val activeMedicationCount = medicationRepository.countActiveByPatientId(id)
//        val pendingReminderCount = reminderRepository.countPendingByPatientId(id)
//        val attachmentCount = attachmentRepository.countByPatientId(id)
//
//        // Return a custom DTO (PatientSummary)
//        return PatientSummary(
//            patient = patient,
//            visits = visitCount,
//            medications = activeMedicationCount,
//            reminders = pendingReminderCount,
//            attachments = attachmentCount
//        )
//    }

    // ------------------- Diff builder for audit -------------------

    private fun buildDiffJson(
        old: Patient?,
        new: Patient?,
        action: String
    ): String? {
        val diff = mutableMapOf<String, Any?>()
        diff["action"] = action

        if (old == null && new != null) {
            // Full snapshot on create
            diff["new"] = mapOf("id" to new.id, "name" to new.name, "species" to new.species) // simplified fields
        } else if (old != null && new == null) {
            // Full snapshot on delete
            diff["old"] = mapOf("id" to old.id, "name" to old.name, "species" to old.species) // simplified fields
        } else if (old != null && new != null) {
            val changes = mutableMapOf<String, Any?>()

            if (old.name != new.name) changes["name"] = mapOf("old" to old.name, "new" to new.name)
            if (old.species != new.species) changes["species"] = mapOf("old" to old.species, "new" to new.species)
            if (old.breed != new.breed) changes["breed"] = mapOf("old" to old.breed, "new" to new.breed)
            // ... (compare all other relevant updatable fields)

            if (changes.isNotEmpty()) diff["changes"] = changes
        }

        return if (diff.size == 1 && diff.containsKey("action")) null else objectMapper.writeValueAsString(diff)
    }
}