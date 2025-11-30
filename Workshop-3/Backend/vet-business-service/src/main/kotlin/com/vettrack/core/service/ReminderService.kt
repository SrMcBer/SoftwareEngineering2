package com.vettrack.core.service

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
    private val userRepository: UserRepository
) {

    fun scheduleReminder(
        patientId: UUID,
        title: String,
        dueAt: OffsetDateTime,
        createdByUserId: UUID?
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

        return reminderRepository.save(reminder)
    }

    fun markDone(reminderId: UUID): Reminder {
        val reminder = reminderRepository.findById(reminderId).orElseThrow {
            NoSuchElementException("Reminder $reminderId not found")
        }
        reminder.markDone()
        return reminderRepository.save(reminder)
    }

    fun listForPatient(patientId: UUID): List<Reminder> =
        reminderRepository.findByPatientId(patientId)

    fun listOverdue(now: OffsetDateTime = OffsetDateTime.now()): List<Reminder> =
        reminderRepository.findByStatusAndDueAtBefore(ReminderStatus.PENDING, now)
}
