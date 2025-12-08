package com.vettrack.core.repository

import com.vettrack.core.domain.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface PatientRepository : JpaRepository<Patient, UUID> {

    fun findByOwnerId(ownerId: UUID): List<Patient>

    fun findByNameContainingIgnoreCase(name: String): List<Patient>

    fun findByMicrochipId(microchipId: String): Patient?
}