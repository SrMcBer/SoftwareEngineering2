package com.vettrack.core.api.exam

import com.vettrack.core.auth.CurrentUserHolder
import com.vettrack.core.domain.ExamTemplate
import com.vettrack.core.service.ExamTemplateService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@Tag(name = "Exam Templates", description = "Manage exam templates")
@RestController
@RequestMapping("/exam-templates")
class ExamTemplateController(
    private val examTemplateService: ExamTemplateService,
    private val currentUserHolder: CurrentUserHolder
) {

    data class CreateExamTemplateRequest(
        @field:NotBlank
        val name: String,
        val description: String? = null,
        val fieldsJson: String,
        val version: Int? = null
    )

    data class ExamTemplateResponse(
        val id: UUID,
        val name: String,
        val description: String?,
        val fieldsJson: String,
        val isActive: Boolean,
        val version: Int,
        val createdByUserId: UUID?,
        val createdAt: java.time.OffsetDateTime,
        val updatedAt: java.time.OffsetDateTime
    )

    @Operation(summary = "Create a new exam template")
    @PostMapping
    fun createTemplate(
        @Valid @RequestBody body: CreateExamTemplateRequest
    ): ResponseEntity<ExamTemplateResponse> {
        val currentUser = currentUserHolder.get()

        val template = examTemplateService.createTemplate(
            name = body.name,
            description = body.description,
            fieldsJson = body.fieldsJson,
            createdByUserId = currentUser?.id,
            version = body.version ?: 1
        )

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(template.toResponse())
    }

    @Operation(summary = "Deactivate an exam template")
    @DeleteMapping("/{templateId}")
    fun deactivateTemplate(@PathVariable templateId: UUID): ExamTemplateResponse {
        val template = examTemplateService.deactivateTemplate(templateId)
        return template.toResponse()
    }

    @Operation(summary = "Get all active exam templates")
    @GetMapping("/active")
    fun getActiveTemplates(): List<ExamTemplateResponse> =
        examTemplateService.getActiveTemplates().map { it.toResponse() }

    @Operation(summary = "Get an exam template by ID")
    @GetMapping("/{templateId}")
    fun getById(@PathVariable templateId: UUID): ExamTemplateResponse =
        examTemplateService.getById(templateId).toResponse()

    private fun ExamTemplate.toResponse(): ExamTemplateResponse =
        ExamTemplateResponse(
            id = this.id!!,
            name = this.name,
            description = this.description,
            fieldsJson = this.fieldsJson,
            isActive = this.isActive,
            version = this.version,
            createdByUserId = this.createdBy?.id,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
}
