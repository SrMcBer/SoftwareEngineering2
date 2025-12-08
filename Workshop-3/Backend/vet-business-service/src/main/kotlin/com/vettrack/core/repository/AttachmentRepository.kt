package com.vettrack.core.repository

import com.vettrack.core.domain.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface AttachmentRepository : JpaRepository<Attachment, UUID> {

    fun findByPatientId(patientId: UUID): List<Attachment>

    fun findByVisitId(visitId: UUID): List<Attachment>

    fun findByExamId(examId: UUID): List<Attachment>
}