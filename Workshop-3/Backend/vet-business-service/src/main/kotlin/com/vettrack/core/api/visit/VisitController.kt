package com.vettrack.core.api.visit

import com.fasterxml.jackson.databind.ObjectMapper
import com.vettrack.core.auth.CurrentUserHolder
import com.vettrack.core.domain.Visit
import com.vettrack.core.service.VisitService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.OffsetDateTime
import java.util.UUID

@RestController
@RequestMapping("/visits")
class VisitController(
    private val visitService: VisitService,
    private val currentUserHolder: CurrentUserHolder,
    private val objectMapper: ObjectMapper
) {
    /**
     * Create a new visit
     *
     * POST /visits
     */
    @PostMapping
    fun createVisit(
        @Valid @RequestBody request: CreateVisitRequest,
        httpServletRequest: HttpServletRequest
    ): ResponseEntity<VisitResponse> {
        val currentUser = currentUserHolder.get()
        val ip = extractIp(httpServletRequest)

        // 1. Build vitalsJson from individual fields using ObjectMapper
        val vitalsJson = buildVitalsJson(request)

        val visit = visitService.createVisit(
            patientId = request.patientId,
            reason = request.reason,
            vitalsJson = vitalsJson,
            examNotes = request.examNotes,
            diagnoses = request.diagnoses,
            procedures = request.procedures,
            recommendations = request.recommendations,
            createdByUserId = currentUser?.id,
            actorUserId = currentUser?.id,
            actorIp = ip
        )

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(visit.toResponse())
    }

    /**
     * Update an existing visit
     *
     * PUT /visits/{id}
     */
    @PutMapping("/{id}")
    fun updateVisit(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateVisitRequest,
        httpServletRequest: HttpServletRequest
    ): VisitResponse {
        val currentUser = currentUserHolder.get()
        val ip = extractIp(httpServletRequest)

        // 1. Build vitalsJson from individual fields if any vital is present
        // Pass the request to the helper, which will intelligently handle nulls.
        val vitalsJson = buildVitalsJson(request)

        val updated = visitService.updateVisit(
            visitId = id,
            reason = request.reason,
            vitalsJson = vitalsJson,
            examNotes = request.examNotes,
            diagnoses = request.diagnoses,
            procedures = request.procedures,
            recommendations = request.recommendations,
            actorUserId = currentUser?.id,
            actorIp = ip
        )

        return updated.toResponse()
    }

    // --- Read/Search Endpoints ---

    @GetMapping("/{id}")
    fun getVisitById(@PathVariable id: UUID): VisitResponse =
        visitService.getById(id).toResponse()

    @GetMapping("/patient/{patientId}")
    fun listVisitsForPatient(@PathVariable patientId: UUID): List<VisitResponse> =
        visitService.listForPatient(patientId).map { it.toResponse() }

    @GetMapping("/patient/{patientId}/last")
    fun lastVisitForPatient(@PathVariable patientId: UUID): ResponseEntity<VisitResponse> {
        val lastVisit = visitService.lastVisitForPatient(patientId)
        return if (lastVisit != null) {
            ResponseEntity.ok(lastVisit.toResponse())
        } else {
            ResponseEntity.noContent().build()
        }
    }

    @GetMapping("/search")
    fun listVisitsBetween(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) start: OffsetDateTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) end: OffsetDateTime
    ): List<VisitResponse> =
        visitService.listBetween(start, end).map { it.toResponse() }

    @GetMapping
    fun listAllVisits(): List<VisitResponse> =
        visitService.listAll().map { it.toResponse() }


    // ---------- helpers ----------

    private fun extractIp(request: HttpServletRequest): String? {
        val forwarded = request.getHeader("X-Forwarded-For")
        return forwarded?.split(",")?.firstOrNull()?.trim()
            ?: request.remoteAddr
    }

    /**
     * Helper to build the vitals JSON string from request DTO fields.
     */
    private fun buildVitalsJson(request: BaseVisitRequest): String? {
        val vitals = mutableMapOf<String, Double>()

        request.weightKg?.let { vitals["weight"] = it }
        request.heartRate?.let { vitals["heart_rate"] = it }
        request.temperatureC?.let { vitals["temperature"] = it }
        request.respiratoryRate?.let { vitals["respiratory_rate"] = it }

        return if (vitals.isNotEmpty()) {
            objectMapper.writeValueAsString(vitals)
        } else {
            null
        }
    }
}

interface BaseVisitRequest {
    val weightKg: Double?
    val heartRate: Double?
    val temperatureC: Double?
    val respiratoryRate: Double?
}

data class CreateVisitRequest(
    @field:NotNull
    val patientId: UUID,

    @field:NotBlank
    @field:Size(max = 255)
    val reason: String,

    val examNotes: String? = null, // Maps to service's 'examNotes'

    // Vital Signs (implements BaseVisitRequest)
    override val weightKg: Double? = null,
    override val heartRate: Double? = null,
    override val temperatureC: Double? = null,
    override val respiratoryRate: Double? = null,

    val diagnoses: String? = null,
    val procedures: String? = null,
    val recommendations: String? = null
) : BaseVisitRequest

data class UpdateVisitRequest(
    val reason: String? = null,
    val examNotes: String? = null,

    // Vital Signs (implements BaseVisitRequest)
    override val weightKg: Double? = null,
    override val heartRate: Double? = null,
    override val temperatureC: Double? = null,
    override val respiratoryRate: Double? = null,

    val diagnoses: String? = null,
    val procedures: String? = null,
    val recommendations: String? = null
) : BaseVisitRequest

data class VisitResponse(
    val id: UUID,
    val patientId: UUID,
    val dateTime: OffsetDateTime, // From Visit.dateTime
    val reason: String,
    val vitalsJson: String?,
    val examNotes: String?,
    val diagnoses: String?,
    val procedures: String?,
    val recommendations: String?,
    val createdByUserId: UUID?,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
)

// Basic visit → response
fun Visit.toResponse(): VisitResponse =
    VisitResponse(
        id = this.id!!,
        patientId = this.patient.id!!,
        dateTime = this.dateTime,
        reason = this.reason!!,
        vitalsJson = this.vitalsJson,
        examNotes = this.examNotes,
        diagnoses = this.diagnoses,
        procedures = this.procedures,
        recommendations = this.recommendations,
        createdByUserId = this.createdBy?.id,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
