package com.vettrack.core.repository

import com.vettrack.core.domain.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import java.util.UUID

@Repository
interface ReminderRepository : JpaRepository<Reminder, UUID> {

    fun findByPatientId(patientId: UUID): List<Reminder>

    fun findByStatus(status: ReminderStatus): List<Reminder>

    fun findByStatusAndDueAtBefore(status: ReminderStatus, before: OffsetDateTime): List<Reminder>
}