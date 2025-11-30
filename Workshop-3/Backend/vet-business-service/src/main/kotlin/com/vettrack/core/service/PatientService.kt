package com.vettrack.core.service

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
    private val ownerRepository: OwnerRepository
) {

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
        notes: String? = null
    ): Patient {
        val owner: Owner = ownerRepository.findById(ownerId).orElseThrow {
            NoSuchElementException("Owner $ownerId not found")
        }

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
            createdAt = OffsetDateTime.now(),
            updatedAt = OffsetDateTime.now()
        )

        return patientRepository.save(patient)
    }

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
}