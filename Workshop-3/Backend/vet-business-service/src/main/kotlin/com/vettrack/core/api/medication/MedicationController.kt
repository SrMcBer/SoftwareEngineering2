package com.vettrack.core.api.medication

import com.vettrack.core.auth.CurrentUserHolder
import com.vettrack.core.domain.DoseEvent
import com.vettrack.core.domain.Medication
import com.vettrack.core.service.MedicationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.UUID

@Tag(name = "Medications", description = "Manage patient medications and dosing")
@RestController
@RequestMapping
class MedicationController(
    private val medicationService: MedicationService,
    private val currentUserHolder: CurrentUserHolder
) {

    data class PrescribeMedicationRequest(
        @field:NotNull
        var patientId: UUID,
        @field:NotBlank
        val name: String,
        val dosage: String? = null,
        val route: String? = null,
        val frequency: String? = null,
        val startDate: LocalDate? = null,
        val endDate: LocalDate? = null
    )

    data class UpdateMedicationRequest(
        val dosage: String? = null,
        val route: String? = null,
        val frequency: String? = null,
        val startDate: LocalDate? = null,
        val endDate: LocalDate? = null
    )

    data class EndMedicationRequest(
        val endDate: LocalDate? = null
    )

    data class RecordDoseRequest(
        val amount: String? = null,
        val notes: String? = null
    )

    data class MedicationResponse(
        val id: UUID,
        val patientId: UUID,
        val name: String,
        val dosage: String?,
        val route: String?,
        val frequency: String?,
        val startDate: LocalDate?,
        val endDate: LocalDate?,
        val isActive: Boolean,
        val nextDueAt: java.time.OffsetDateTime?,
        val lastAdministeredAt: java.time.OffsetDateTime?,
        val createdByUserId: UUID?,
        val createdAt: java.time.OffsetDateTime,
        val updatedAt: java.time.OffsetDateTime
    )

    data class DoseEventResponse(
        val id: UUID,
        val medicationId: UUID,
        val occurredAt: java.time.OffsetDateTime,
        val amount: String?,
        val notes: String?,
        val recordedByUserId: UUID?
    )

    // --- Prescribe ---

    @Operation(summary = "Prescribe a new medication for a patient")
    @PostMapping("/medications")
    fun prescribeMedication(
        @Valid @RequestBody body: PrescribeMedicationRequest,
        request: HttpServletRequest
    ): ResponseEntity<MedicationResponse> {
        val currentUser = currentUserHolder.get()
        val actorIp = extractIp(request)

        val med = medicationService.prescribeMedication(
            patientId = body.patientId,
            name = body.name,
            dosage = body.dosage,
            route = body.route,
            frequency = body.frequency,
            startDate = body.startDate,
            endDate = body.endDate,
            createdByUserId = currentUser?.id,
            actorUserId = currentUser?.id,
            actorIp = actorIp
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(med.toResponse())
    }

    // --- Update prescription ---

    @Operation(summary = "Update an existing medication prescription")
    @PutMapping("/medications/{medicationId}")
    fun updateMedication(
        @PathVariable medicationId: UUID,
        @RequestBody body: UpdateMedicationRequest,
        request: HttpServletRequest
    ): MedicationResponse {
        val currentUser = currentUserHolder.get()
        val actorIp = extractIp(request)

        val med = medicationService.updateMedication(
            medicationId = medicationId,
            dosage = body.dosage,
            route = body.route,
            frequency = body.frequency,
            startDate = body.startDate,
            endDate = body.endDate,
            actorUserId = currentUser?.id,
            actorIp = actorIp
        )

        return med.toResponse()
    }

    // --- End medication ---

    @Operation(summary = "End a medication course")
    @PostMapping("/medications/{medicationId}/end")
    fun endMedication(
        @PathVariable medicationId: UUID,
        @RequestBody(required = false) body: EndMedicationRequest?,
        request: HttpServletRequest
    ): MedicationResponse {
        val currentUser = currentUserHolder.get()
        val actorIp = extractIp(request)
        val endDate = body?.endDate ?: LocalDate.now()

        val med = medicationService.endMedication(
            medicationId = medicationId,
            endDate = endDate,
            actorUserId = currentUser?.id,
            actorIp = actorIp
        )

        return med.toResponse()
    }

    // --- Record dose ---

    @Operation(summary = "Record a dose for a medication")
    @PostMapping("/medications/{medicationId}/doses")
    fun recordDose(
        @PathVariable medicationId: UUID,
        @RequestBody body: RecordDoseRequest,
        request: HttpServletRequest
    ): ResponseEntity<DoseEventResponse> {
        val currentUser = currentUserHolder.get()
        val actorIp = extractIp(request)

        val dose = medicationService.recordDose(
            medicationId = medicationId,
            amount = body.amount,
            notes = body.notes,
            recordedByUserId = currentUser?.id,
            actorIp = actorIp
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(dose.toResponse())
    }

    // --- Read ---

    @Operation(summary = "List all medications for a patient (active and inactive)")
    @GetMapping("/patients/{patientId}/medications")
    fun listMedicationsForPatient(
        @PathVariable patientId: UUID
    ): List<MedicationResponse> =
        medicationService.listMedicationsForPatient(patientId).map { it.toResponse() }

    @Operation(summary = "List all dose events for a medication")
    @GetMapping("/medications/{medicationId}/doses")
    fun listDoseEvents(
        @PathVariable medicationId: UUID
    ): List<DoseEventResponse> =
        medicationService.listDoseEvents(medicationId).map { it.toResponse() }


    private fun extractIp(request: HttpServletRequest): String? {
        val forwarded = request.getHeader("X-Forwarded-For")
        return forwarded?.split(",")?.firstOrNull()?.trim()
            ?: request.remoteAddr
    }

    private fun Medication.toResponse(): MedicationResponse =
        MedicationResponse(
            id = this.id!!,
            patientId = this.patient.id!!,
            name = this.name,
            dosage = this.dosage,
            route = this.route,
            frequency = this.frequency,
            startDate = this.startDate,
            endDate = this.endDate,
            isActive = this.isActive(),
            createdByUserId = this.createdBy?.id,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            nextDueAt = this.nextDueAt,
            lastAdministeredAt = this.lastAdministeredAt
        )

    private fun DoseEvent.toResponse(): DoseEventResponse =
        DoseEventResponse(
            id = this.id!!,
            medicationId = this.medication.id!!,
            occurredAt = this.administeredAt,
            amount = this.amount,
            notes = this.notes,
            recordedByUserId = this.recordedBy?.id
        )
}