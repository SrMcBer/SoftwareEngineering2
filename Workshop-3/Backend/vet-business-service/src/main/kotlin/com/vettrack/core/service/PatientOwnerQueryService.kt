package com.vettrack.core.service

import com.vettrack.core.domain.Owner
import com.vettrack.core.domain.Patient
import com.vettrack.core.repository.PatientRepository
import com.vettrack.core.repository.OwnerRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class PatientOwnerQueryService(
    private val patientRepository: PatientRepository,
    private val ownerRepository: OwnerRepository
) {
    fun getPatientById(id: UUID): Patient? = patientRepository.findById(id).orElse(null)
    fun getOwnerById(id: UUID): Owner = ownerRepository.findById(id).orElseThrow {
        NoSuchElementException("Owner $id not found")
    }
    fun getByOwnerId(id: UUID): List<Patient> = patientRepository.findByOwnerId(id)
}