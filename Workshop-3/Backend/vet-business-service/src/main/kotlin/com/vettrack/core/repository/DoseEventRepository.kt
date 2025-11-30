package com.vettrack.core.repository

import com.vettrack.core.domain.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface DoseEventRepository : JpaRepository<DoseEvent, UUID> {

    fun findByMedicationIdOrderByAdministeredAtDesc(medicationId: UUID): List<DoseEvent>
}