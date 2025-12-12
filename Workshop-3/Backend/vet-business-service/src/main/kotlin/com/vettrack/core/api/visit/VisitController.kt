package com.vettrack.core.api.visit

import com.fasterxml.jackson.databind.ObjectMapper
import com.vettrack.core.auth.CurrentUserHolder
import com.vettrack.core.domain.Visit
import com.vettrack.core.service.AttachmentService
import com.vettrack.core.service.ExamService
import com.vettrack.core.service.MedicationService
import com.vettrack.core.service.VisitService
import com.vettrack.core.domain.Patient
import com.vettrack.core.domain.Medication
import com.vettrack.core.domain.Exam
import com.vettrack.core.domain.Attachment
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
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import java.time.LocalDate

@Tag(name = "Visits", description = "Operations related to patient visits")
@RestController
@RequestMapping("/visits")
class VisitController(
    private val visitService: VisitService,
    private val currentUserHolder: CurrentUserHolder,
    private val objectMapper: ObjectMapper,
    private val examService: ExamService,
    private val medicationService: MedicationService,
    private val attachmentService: AttachmentService
) {

    @Operation(summary = "Create a visit")
    @PostMapping
    fun createVisit(
        @Valid @RequestBody request: CreateVisitRequest,
        httpServletRequest: HttpServletRequest
    ): ResponseEntity<VisitResponse> {
        val currentUser = currentUserHolder.get()
        val ip = extractIp(httpServletRequest)

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

    @Operation(summary = "Update an existing visit")
    @PutMapping("/{id}")
    fun updateVisit(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateVisitRequest,
        httpServletRequest: HttpServletRequest
    ): VisitResponse {
        val currentUser = currentUserHolder.get()
        val ip = extractIp(httpServletRequest)

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

    @Operation(summary = "Get a visit by ID")
    @GetMapping("/{id}")
    fun getVisitById(@PathVariable id: UUID): VisitResponse =
        visitService.getById(id).toResponse()

    /**
     * Get a visit with all related clinical details
     *
     * GET /visits/{id}/details
     *
     * Returns:
     *  - Visit core data
     *  - Patient summary
     *  - Exams for this visit
     *  - Active medications for the patient
     *  - Attachments linked to this visit
     */
    @Operation(
        summary = "Get full details for a visit",
        description = "Fetches a visit together with patient summary, exams, medications and attachments in a single call."
    )
    @GetMapping("/{id}/details")
    fun getVisitDetails(
        @PathVariable id: UUID
    ): VisitDetailsResponse {
        // 1. Core visit (includes patient)
        val visit: Visit = visitService.getById(id)
        val patient: Patient = visit.patient

        // 2. Exams *for this visit*
        val examsForVisit: List<Exam> =
            examService.listForPatient(patient.id!!)
                .filter { it.visit?.id == visit.id }

        // 3. Active medications for this patient (current long-term meds)
        val medicationsForPatient: List<Medication> =
            medicationService.listMedicationsForPatient(patient.id!!)

        // 4. Attachments for this visit (assuming such a method exists)
        val attachmentsForVisit: List<Attachment> =
            attachmentService.listForVisit(visit.id!!)

        return VisitDetailsResponse(
            visit = visit.toResponse(),
            patient = patient.toVisitPatientSummary(),
            exams = examsForVisit.map { it.toVisitExamSummary() },
            medications = medicationsForPatient.map { it.toVisitMedicationSummary() },
            attachments = attachmentsForVisit.map { it.toVisitAttachmentSummary() }
        )
    }

    @Operation(summary = "List visits for a patient")
    @GetMapping("/patient/{patientId}")
    fun listVisitsForPatient(@PathVariable patientId: UUID): List<VisitResponse> =
        visitService.listForPatient(patientId).map { it.toResponse() }

    @Operation(summary = "Get last visit for a patient")
    @GetMapping("/patient/{patientId}/last")
    fun lastVisitForPatient(@PathVariable patientId: UUID): ResponseEntity<VisitResponse> {
        val lastVisit = visitService.lastVisitForPatient(patientId)
        return if (lastVisit != null) {
            ResponseEntity.ok(lastVisit.toResponse())
        } else {
            ResponseEntity.noContent().build()
        }
    }

    @Operation(summary = "List visits between two datetimes")
    @GetMapping("/search")
    fun listVisitsBetween(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) start: OffsetDateTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) end: OffsetDateTime
    ): List<VisitResponse> =
        visitService.listBetween(start, end).map { it.toResponse() }

    @Operation(summary = "List all visits")
    @GetMapping
    fun listAllVisits(): List<VisitResponse> =
        visitService.listAll().map { it.toResponse() }

    // ---------- helpers ----------

    private fun extractIp(request: HttpServletRequest): String? {
        val forwarded = request.getHeader("X-Forwarded-For")
        return forwarded?.split(",")?.firstOrNull()?.trim()
            ?: request.remoteAddr
    }

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
    var patientId: UUID,

    @field:NotBlank
    @field:Size(max = 255)
    val reason: String,

    val examNotes: String? = null,
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
    val dateTime: OffsetDateTime,
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

data class VisitDetailsResponse(
    val visit: VisitResponse,
    val patient: VisitPatientSummary,
    val exams: List<VisitExamSummary>,
    val medications: List<VisitMedicationSummary>,
    val attachments: List<VisitAttachmentSummary>
)

// --- Patient summary (embedded in visit details) ---

data class VisitPatientSummary(
    val id: UUID,
    val ownerId: UUID?,
    val name: String,
    val species: String,
    val breed: String?,
    val sex: String?,
    val dob: LocalDate?,
    val color: String?
)

fun Patient.toVisitPatientSummary(): VisitPatientSummary =
    VisitPatientSummary(
        id = this.id!!,
        ownerId = this.owner?.id,
        name = this.name,
        species = this.species,
        breed = this.breed,
        sex = this.sex,
        dob = this.dob,
        color = this.color
    )

// --- Exam summary ---

data class VisitExamSummary(
    val id: UUID,
    val templateId: UUID,
    val templateName: String,
    val status: String,
    val performedAt: OffsetDateTime,
    val performedByUserId: UUID?,
    val vitalsJson: String?,
    val resultsJson: String?
)

fun Exam.toVisitExamSummary(): VisitExamSummary =
    VisitExamSummary(
        id = this.id!!,
        templateId = this.template.id!!,
        templateName = this.template.name,
        status = this.status.name,
        performedAt = this.performedAt,
        performedByUserId = this.performedBy?.id,
        vitalsJson = this.vitalsJson,
        resultsJson = this.resultsJson
    )

// --- Medication summary ---

data class VisitMedicationSummary(
    val id: UUID,
    val name: String,
    val dosage: String?,
    val route: String?,
    val frequency: String?,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val lastAdministeredAt: OffsetDateTime?,
    val nextDueAt: OffsetDateTime?
)

fun Medication.toVisitMedicationSummary(): VisitMedicationSummary =
    VisitMedicationSummary(
        id = this.id!!,
        name = this.name,
        dosage = this.dosage,
        route = this.route,
        frequency = this.frequency,
        startDate = this.startDate,
        endDate = this.endDate,
        lastAdministeredAt = this.lastAdministeredAt,
        nextDueAt = this.nextDueAt
    )

// --- Attachment summary ---
// Adjust fields to match your Attachment domain

data class VisitAttachmentSummary(
    val id: UUID,
    val type: String?,
    val filename: String?,
    val url: String?,
    val createdAt: OffsetDateTime
)

fun Attachment.toVisitAttachmentSummary(): VisitAttachmentSummary =
    VisitAttachmentSummary(
        id = this.id!!,
        type = this.type,
        filename = this.filename,
        createdAt = this.uploadedAt,
        url = this.url,
    )

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
