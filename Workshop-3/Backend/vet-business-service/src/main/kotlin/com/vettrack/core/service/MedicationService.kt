package com.vettrack.core.service

import com.vettrack.core.domain.DoseEvent
import com.vettrack.core.domain.Medication
import com.vettrack.core.domain.Patient
import com.vettrack.core.domain.User
import com.vettrack.core.repository.DoseEventRepository
import com.vettrack.core.repository.MedicationRepository
import com.vettrack.core.repository.PatientRepository
import com.vettrack.core.repository.UserRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@Service
class MedicationService(
    private val medicationRepository: MedicationRepository,
    private val doseEventRepository: DoseEventRepository,
    private val patientRepository: PatientRepository,
    private val userRepository: UserRepository
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
        val patient: Patient = patientRepository.findById(patientId).orElseThrow {
            NoSuchElementException("Patient $patientId not found")
        }

        val createdBy: User? = createdByUserId?.let { uid ->
            userRepository.findById(uid).orElseThrow {
                NoSuchElementException("User $uid not found")
            }
        }

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

    fun endMedication(medicationId: UUID, endDate: LocalDate = LocalDate.now()): Medication {
        val med = medicationRepository.findById(medicationId).orElseThrow {
            NoSuchElementException("Medication $medicationId not found")
        }
        med.endDate = endDate
        med.updatedAt = OffsetDateTime.now()
        return medicationRepository.save(med)
    }

    fun recordDose(
        medicationId: UUID,
        amount: String?,
        notes: String?,
        recordedByUserId: UUID?
    ): DoseEvent {
        val med = medicationRepository.findById(medicationId).orElseThrow {
            NoSuchElementException("Medication $medicationId not found")
        }

        val recordedBy: User? = recordedByUserId?.let { uid ->
            userRepository.findById(uid).orElseThrow {
                NoSuchElementException("User $uid not found")
            }
        }

        val now = OffsetDateTime.now()
        val doseEvent = DoseEvent(
            medication = med,
            administeredAt = now,
            amount = amount,
            notes = notes,
            recordedBy = recordedBy
        )

        med.lastAdministeredAt = now
        med.updatedAt = now
        medicationRepository.save(med)

        return doseEventRepository.save(doseEvent)
    }

    fun listActiveForPatient(patientId: UUID): List<Medication> =
        medicationRepository.findByPatientIdAndEndDateIsNull(patientId)
}
