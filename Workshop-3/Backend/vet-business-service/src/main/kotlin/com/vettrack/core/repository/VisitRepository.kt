package com.vettrack.core.repository

import com.vettrack.core.domain.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import java.util.UUID

@Repository
interface VisitRepository : JpaRepository<Visit, UUID> {

    fun findByPatientIdOrderByDateTimeDesc(patientId: UUID): List<Visit>

    fun findFirstByPatientIdOrderByDateTimeDesc(patientId: UUID): Visit?

    fun findByDateTimeBetween(start: OffsetDateTime, end: OffsetDateTime): List<Visit>
}