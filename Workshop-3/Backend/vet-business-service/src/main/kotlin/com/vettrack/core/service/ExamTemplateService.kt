package com.vettrack.core.service

import com.vettrack.core.domain.ExamTemplate
import com.vettrack.core.domain.User
import com.vettrack.core.repository.ExamTemplateRepository
import com.vettrack.core.repository.UserRepository
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.UUID

@Service
class ExamTemplateService(
    private val examTemplateRepository: ExamTemplateRepository,
    private val userRepository: UserRepository
) {

    fun createTemplate(
        name: String,
        description: String?,
        fieldsJson: String,
        createdByUserId: UUID?,
        version: Int = 1
    ): ExamTemplate {
        val createdBy: User? = createdByUserId?.let { uid ->
            userRepository.findById(uid).orElseThrow {
                NoSuchElementException("User $uid not found")
            }
        }

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
}
