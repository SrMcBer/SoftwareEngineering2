package com.vettrack.core.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.vettrack.core.domain.ExamTemplate
import com.vettrack.core.domain.User
import com.vettrack.core.repository.ExamTemplateRepository
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.UUID

@Service
class ExamTemplateService(
    private val examTemplateRepository: ExamTemplateRepository,
    private val userService: UserService,
    private val objectMapper: ObjectMapper
) {

    fun createTemplate(
        name: String,
        description: String?,
        fieldsJson: String,
        createdByUserId: UUID?,
        version: Int = 1
    ): ExamTemplate {
        validateFieldSchema(fieldsJson)

        val createdBy: User? = createdByUserId?.let { uid -> userService.getById(uid) }

        val now = OffsetDateTime.now()
        val template = ExamTemplate(
            name = name,
            version = version,
            description = description,
            isActive = true,
            fieldsJson = fieldsJson,
            createdBy = createdBy,
            createdAt = now,
            updatedAt = now
        )

        return examTemplateRepository.save(template)
    }

    fun deactivateTemplate(templateId: UUID): ExamTemplate {
        val template = examTemplateRepository.findById(templateId).orElseThrow {
            NoSuchElementException("Template $templateId not found")
        }
        template.isActive = false
        template.updatedAt = OffsetDateTime.now()
        return examTemplateRepository.save(template)
    }

    fun getActiveTemplates(): List<ExamTemplate> =
        examTemplateRepository.findByIsActiveTrue()

    private fun validateFieldSchema(fieldsJson: String) {
        try {
            objectMapper.readTree(fieldsJson)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid fieldsJson schema format: ${e.message}")
        }
    }

    // Add getById back in for ExamService collaboration
    fun getById(templateId: UUID): ExamTemplate =
        examTemplateRepository.findById(templateId).orElseThrow {
            NoSuchElementException("Template $templateId not found")
        }
}
