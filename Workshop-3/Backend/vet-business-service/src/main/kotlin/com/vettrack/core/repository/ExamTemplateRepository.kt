package com.vettrack.core.repository

import com.vettrack.core.domain.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ExamTemplateRepository : JpaRepository<ExamTemplate, UUID> {

    fun findByIsActiveTrue(): List<ExamTemplate>

    fun findByNameIgnoreCase(name: String): List<ExamTemplate>

    fun findByNameIgnoreCaseAndIsActiveTrue(name: String): List<ExamTemplate>
}