package com.vettrack.core.service

import com.vettrack.core.domain.*
import com.vettrack.core.repository.ExamRepository
import com.vettrack.core.repository.ExamTemplateRepository
import com.vettrack.core.repository.PatientRepository
import com.vettrack.core.repository.UserRepository
import com.vettrack.core.repository.VisitRepository
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.UUID

@Service
class ExamService(
    private val examRepository: ExamRepository,
    private val examTemplateRepository: ExamTemplateRepository,
    private val patientRepository: PatientRepository,
    private val visitRepository: VisitRepository,
    private val userRepository: UserRepository
) {

    fun createExamFromTemplate(
        patientId: UUID,
        visitId: UUID,
        templateId: UUID,
        performedByUserId: UUID?,
        vitalsJson: String? = null,
        resultsJson: String,
        status: ExamStatus = ExamStatus.DRAFT
    ): Exam {
        val patient = patientRepository.findById(patientId).orElseThrow {
            NoSuchElementException("Patient $patientId not found")
        }

        val visit = visitRepository.findById(visitId).orElseThrow {
            NoSuchElementException("Visit $visitId not found")
        }

        require(visit.patient.id == patient.id) {
            "Visit $visitId does not belong to patient $patientId"
        }

        val template = examTemplateRepository.findById(templateId).orElseThrow {
            NoSuchElementException("Template $templateId not found")
        }

        val performedBy: User? = performedByUserId?.let { uid ->
            userRepository.findById(uid).orElseThrow {
                NoSuchElementException("User $uid not found")
            }
        }

        val now = OffsetDateTime.now()
        val exam = Exam(
            patient = patient,
            visit = visit,
            template = template,
            templateVersion = template.version,
            performedAt = now,
            performedBy = performedBy,
            vitalsJson = vitalsJson,
            resultsJson = resultsJson,
            status = status,
            createdAt = now,
            updatedAt = now,
        )

        return examRepository.save(exam)
    }

    fun finalizeExam(examId: UUID): Exam {
        val exam = examRepository.findById(examId).orElseThrow {
            NoSuchElementException("Exam $examId not found")
        }
        exam.finish()
        return examRepository.save(exam)
    }

    fun listForPatient(patientId: UUID): List<Exam> =
        examRepository.findByPatientIdOrderByPerformedAtDesc(patientId)
}
