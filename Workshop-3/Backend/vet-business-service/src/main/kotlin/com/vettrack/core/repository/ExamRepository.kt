package com.vettrack.core.repository

import com.vettrack.core.domain.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ExamRepository : JpaRepository<Exam, UUID> {

    fun findByPatientIdOrderByPerformedAtDesc(patientId: UUID): List<Exam>

    fun findByVisitId(visitId: UUID): List<Exam>

    fun findByTemplateId(templateId: UUID): List<Exam>

    fun findByStatus(status: ExamStatus): List<Exam>
}