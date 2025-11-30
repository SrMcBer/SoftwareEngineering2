package com.vettrack.core.service

import com.vettrack.core.domain.*
import com.vettrack.core.repository.*
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.UUID

@Service
class AttachmentService(
    private val attachmentRepository: AttachmentRepository,
    private val patientRepository: PatientRepository,
    private val visitRepository: VisitRepository,
    private val examRepository: ExamRepository,
    private val userRepository: UserRepository
) {

    fun uploadAttachmentToVisit(
        patientId: UUID,
        visitId: UUID,
        type: String,
        url: String,
        filename: String?,
        uploadedByUserId: UUID?
    ): Attachment {
        val patient = patientRepository.findById(patientId).orElseThrow {
            NoSuchElementException("Patient $patientId not found")
        }
        val visit = visitRepository.findById(visitId).orElseThrow {
            NoSuchElementException("Visit $visitId not found")
        }
        val uploadedBy: User? = uploadedByUserId?.let { uid ->
            userRepository.findById(uid).orElseThrow {
                NoSuchElementException("User $uid not found")
            }
        }

        val now = OffsetDateTime.now()
        val attachment = Attachment(
            patient = patient,
            visit = visit,
            exam = null,
            type = type,
            url = url,
            filename = filename,
            uploadedBy = uploadedBy,
            uploadedAt = now
        )

        return attachmentRepository.save(attachment)
    }

    fun uploadAttachmentToExam(
        patientId: UUID,
        examId: UUID,
        type: String,
        url: String,
        filename: String?,
        uploadedByUserId: UUID?
    ): Attachment {
        val patient = patientRepository.findById(patientId).orElseThrow {
            NoSuchElementException("Patient $patientId not found")
        }
        val exam = examRepository.findById(examId).orElseThrow {
            NoSuchElementException("Exam $examId not found")
        }
        val uploadedBy: User? = uploadedByUserId?.let { uid ->
            userRepository.findById(uid).orElseThrow {
                NoSuchElementException("User $uid not found")
            }
        }

        val now = OffsetDateTime.now()
        val attachment = Attachment(
            patient = patient,
            visit = null,
            exam = exam,
            type = type,
            url = url,
            filename = filename,
            uploadedBy = uploadedBy,
            uploadedAt = now
        )

        return attachmentRepository.save(attachment)
    }

    fun listForPatient(patientId: UUID): List<Attachment> =
        attachmentRepository.findByPatientId(patientId)

    fun listForVisit(visitId: UUID): List<Attachment> =
        attachmentRepository.findByVisitId(visitId)

    fun listForExam(examId: UUID): List<Attachment> =
        attachmentRepository.findByExamId(examId)
}
