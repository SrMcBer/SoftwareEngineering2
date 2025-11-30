package com.vettrack.core.service

import com.vettrack.core.domain.Patient
import com.vettrack.core.domain.User
import com.vettrack.core.domain.Visit
import com.vettrack.core.repository.PatientRepository
import com.vettrack.core.repository.UserRepository
import com.vettrack.core.repository.VisitRepository
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.UUID

@Service
class VisitService(
    private val visitRepository: VisitRepository,
    private val patientRepository: PatientRepository,
    private val userRepository: UserRepository
) {

    fun createVisit(
        patientId: UUID,
        reason: String?,
        vitalsJson: String?,
        examNotes: String?,
        diagnoses: String?,
        procedures: String?,
        recommendations: String?,
        createdByUserId: UUID?
    ): Visit {
        val patient: Patient = patientRepository.findById(patientId).orElseThrow {
            NoSuchElementException("Patient $patientId not found")
        }

        val createdBy: User? = createdByUserId?.let { uid ->
            userRepository.findById(uid).orElseThrow {
                NoSuchElementException("User $uid not found")
            }
        }

        val now = OffsetDateTime.now()
        val visit = Visit(
            patient = patient,
            dateTime = now,
            reason = reason,
            vitalsJson = vitalsJson,
            examNotes = examNotes,
            diagnoses = diagnoses,
            procedures = procedures,
            recommendations = recommendations,
            createdBy = createdBy,
            createdAt = now,
            updatedAt = now
        )

        return visitRepository.save(visit)
    }

    fun getById(id: UUID): Visit =
        visitRepository.findById(id).orElseThrow {
            NoSuchElementException("Visit $id not found")
        }

    fun listForPatient(patientId: UUID): List<Visit> =
        visitRepository.findByPatientIdOrderByDateTimeDesc(patientId)

    fun lastVisitForPatient(patientId: UUID): Visit? =
        visitRepository.findFirstByPatientIdOrderByDateTimeDesc(patientId)

    fun listBetween(start: OffsetDateTime, end: OffsetDateTime): List<Visit> =
        visitRepository.findByDateTimeBetween(start, end)
}
