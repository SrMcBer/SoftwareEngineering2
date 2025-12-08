package com.vettrack.core.api.exam

import com.vettrack.core.auth.CurrentUserHolder
import com.vettrack.core.domain.Exam
import com.vettrack.core.domain.ExamStatus
import com.vettrack.core.service.ExamService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@Tag(name = "Exams", description = "Operations related to medical exams")
@RestController
@RequestMapping("/exams")
class ExamController(
    private val examService: ExamService,
    private val currentUserHolder: CurrentUserHolder
) {

    data class CreateExamFromTemplateRequest(
        @field:NotNull
        var patientId: UUID,
        @field:NotNull
        var visitId: UUID,
        @field:NotNull
        var templateId: UUID,
        val vitalsJson: String? = null,
        val resultsJson: String,
        val status: ExamStatus? = null
    )

    data class UpdateExamRequest(
        val vitalsJson: String? = null,
        val resultsJson: String? = null
    )

    data class ExamResponse(
        val id: UUID,
        val patientId: UUID,
        val visitId: UUID,
        val templateId: UUID?,
        val status: ExamStatus,
        val vitalsJson: String?,
        val resultsJson: String?,
        val createdByUserId: UUID?,
        val createdAt: java.time.OffsetDateTime,
        val updatedAt: java.time.OffsetDateTime
    )

    @Operation(summary = "Create an exam from a template")
    @PostMapping("/from-template")
    fun createExamFromTemplate(
        @Valid @RequestBody body: CreateExamFromTemplateRequest,
        request: HttpServletRequest
    ): ResponseEntity<ExamResponse> {
        val currentUser = currentUserHolder.get()
        val actorIp = extractIp(request)

        val exam = examService.createExamFromTemplate(
            patientId = body.patientId,
            visitId = body.visitId,
            templateId = body.templateId,
            performedByUserId = currentUser?.id,
            vitalsJson = body.vitalsJson,
            resultsJson = body.resultsJson,
            status = body.status ?: ExamStatus.draft,
            actorUserId = currentUser?.id,
            actorIp = actorIp
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(exam.toResponse())
    }

    @Operation(summary = "Update an existing exam")
    @PutMapping("/{examId}")
    fun updateExam(
        @PathVariable examId: UUID,
        @RequestBody body: UpdateExamRequest,
        request: HttpServletRequest
    ): ExamResponse {
        val currentUser = currentUserHolder.get()
        val actorIp = extractIp(request)

        val exam = examService.updateExam(
            examId = examId,
            vitalsJson = body.vitalsJson,
            resultsJson = body.resultsJson,
            actorUserId = currentUser?.id,
            actorIp = actorIp
        )

        return exam.toResponse()
    }

    @Operation(summary = "Finalize an exam")
    @PostMapping("/{examId}/finalize")
    fun finalizeExam(
        @PathVariable examId: UUID,
        request: HttpServletRequest
    ): ExamResponse {
        val currentUser = currentUserHolder.get()
        val actorIp = extractIp(request)

        val exam = examService.finalizeExam(
            examId = examId,
            actorUserId = currentUser?.id,
            actorIp = actorIp
        )

        return exam.toResponse()
    }

    @Operation(summary = "Get an exam by ID")
    @GetMapping("/{examId}")
    fun getExamById(@PathVariable examId: UUID): ExamResponse =
        examService.getExamById(examId).toResponse()

    @Operation(summary = "List exams for a patient")
    @GetMapping("/patient/{patientId}")
    fun listForPatient(@PathVariable patientId: UUID): List<ExamResponse> =
        examService.listForPatient(patientId).map { it.toResponse() }

    private fun extractIp(request: HttpServletRequest): String? {
        val forwarded = request.getHeader("X-Forwarded-For")
        return forwarded?.split(",")?.firstOrNull()?.trim()
            ?: request.remoteAddr
    }

    private fun Exam.toResponse(): ExamResponse =
        ExamResponse(
            id = this.id!!,
            patientId = this.patient.id!!,
            visitId = this.visit?.id!!,
            templateId = this.template.id,
            status = this.status,
            vitalsJson = this.vitalsJson,
            resultsJson = this.resultsJson,
            createdByUserId = this.performedBy?.id!!,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
}
