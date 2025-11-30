package com.vettrack.core.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.vettrack.core.domain.Patient
import com.vettrack.core.domain.Reminder
import com.vettrack.core.domain.ReminderStatus
import com.vettrack.core.domain.User
import com.vettrack.core.repository.PatientRepository
import com.vettrack.core.repository.ReminderRepository
import com.vettrack.core.repository.UserRepository
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.UUID

@Service
class ReminderService(
    private val reminderRepository: ReminderRepository,
    private val patientRepository: PatientRepository,
    private val userRepository: UserRepository,
    private val auditLogService: AuditLogService,
    private val objectMapper: ObjectMapper
) {

    // ------------------- Creation (CRUD) -------------------

    fun scheduleReminder(
        patientId: UUID,
        title: String,
        dueAt: OffsetDateTime,
        createdByUserId: UUID?,
        actorUserId: UUID?, // Audit actor
        actorIp: String?    // Audit IP
    ): Reminder {
        val patient: Patient = patientRepository.findById(patientId).orElseThrow {
            NoSuchElementException("Patient $patientId not found")
        }

        val createdBy: User? = createdByUserId?.let { uid ->
            userRepository.findById(uid).orElseThrow {
                NoSuchElementException("User $uid not found")
            }
        }

        val now = OffsetDateTime.now()
        val reminder = Reminder(
            patient = patient,
            title = title,
            dueAt = dueAt,
            status = ReminderStatus.PENDING,
            createdBy = createdBy,
            createdAt = now,
            updatedAt = now
        )

        val saved = reminderRepository.save(reminder)

        // --- Audit Logging ---
        val diffJson = buildDiffJson(old = null, new = saved, action = "create")
        auditLogService.log(
            actorUserId = actorUserId,
            entityType = "reminder",
            entityId = saved.id!!,
            action = "create",
            diffSnapshotJson = diffJson,
            ip = actorIp
        )

        // RESPONSIBILITY: trigger notifications (Placeholder)
        triggerNotification(saved)

        return saved
    }

    // ------------------- Status Updates (Mark done/dismissed) -------------------

    fun markDone(
        reminderId: UUID,
        actorUserId: UUID?,
        actorIp: String?
    ): Reminder {
        val reminder = reminderRepository.findById(reminderId).orElseThrow {
            NoSuchElementException("Reminder $reminderId not found")
        }

        if (reminder.status == ReminderStatus.DONE) return reminder

        val before = reminder.shallowCopy()
        reminder.markDone()
        val saved = reminderRepository.save(reminder)

        // --- Audit Logging ---
        val diffJson = buildDiffJson(old = before, new = saved, action = "mark_done")
        auditLogService.log(
            actorUserId = actorUserId,
            entityType = "reminder",
            entityId = saved.id!!,
            action = "mark_done",
            diffSnapshotJson = diffJson,
            ip = actorIp
        )
        return saved
    }

    fun dismissReminder( // Implements "Mark dismissed"
        reminderId: UUID,
        actorUserId: UUID?,
        actorIp: String?
    ): Reminder {
        val reminder = reminderRepository.findById(reminderId).orElseThrow {
            NoSuchElementException("Reminder $reminderId not found")
        }

        if (reminder.status == ReminderStatus.DISMISSED) return reminder

        val before = reminder.shallowCopy()
        reminder.markDismissed() // Requires update to Reminder entity/status enum
        val saved = reminderRepository.save(reminder)

        // --- Audit Logging ---
        val diffJson = buildDiffJson(old = before, new = saved, action = "dismiss")
        auditLogService.log(
            actorUserId = actorUserId,
            entityType = "reminder",
            entityId = saved.id!!,
            action = "dismiss",
            diffSnapshotJson = diffJson,
            ip = actorIp
        )
        return saved
    }

    // ------------------- Read/Search (Compute overdue) -------------------

    fun listForPatient(patientId: UUID): List<Reminder> =
        reminderRepository.findByPatientId(patientId)

    fun listOverdue(now: OffsetDateTime = OffsetDateTime.now()): List<Reminder> =
        reminderRepository.findByStatusAndDueAtBefore(ReminderStatus.PENDING, now)


    // ------------------- Helper Methods -------------------

    // RESPONSIBILITY: trigger notifications (Placeholder)
    private fun triggerNotification(reminder: Reminder) {
        // Here you would integrate with a separate notification service (e.g., Kafka, Email/SMS client)
        println("INFO: Triggering notification for reminder: ${reminder.title} due at ${reminder.dueAt}")
    }

    private fun buildDiffJson(
        old: Reminder?,
        new: Reminder?,
        action: String
    ): String? {
        val diff = mutableMapOf<String, Any?>()
        diff["action"] = action

        // Implementation of snapshot/diff logic similar to Owner/Exam services
        if (old != null && new != null) {
            val changes = mutableMapOf<String, Any?>()

            if (old.status != new.status) {
                changes["status"] = mapOf("old" to old.status.name, "new" to new.status.name)
            }
            if (old.dueAt != new.dueAt) {
                changes["dueAt"] = mapOf("old" to old.dueAt, "new" to new.dueAt)
            }
            // ... compare other relevant fields (title, etc.)

            if (changes.isNotEmpty()) {
                diff["changes"] = changes
            }
        }

        return if (diff.size == 1 && diff.containsKey("action")) null else objectMapper.writeValueAsString(diff)
    }
}
