package com.vettrack.core.api.patient

import com.vettrack.core.auth.CurrentUserHolder
import com.vettrack.core.domain.Patient
import com.vettrack.core.service.PatientService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.UUID

@Tag(name = "Patients", description = "Operations related to patients")
@RestController
@RequestMapping("/patients")
class PatientController(
    private val patientService: PatientService,
    private val currentUserHolder: CurrentUserHolder
) {

    @Operation(summary = "Register a new patient")
    @PostMapping
    fun registerPatient(
        @Valid @RequestBody request: RegisterPatientRequest,
        httpServletRequest: HttpServletRequest
    ): ResponseEntity<PatientResponse> {
        val currentUser = currentUserHolder.get()
        val ip = extractIp(httpServletRequest)

        val patient = patientService.registerPatient(
            ownerId = request.ownerId,
            name = request.name,
            species = request.species,
            breed = request.breed,
            sex = request.sex,
            dob = request.dob,
            color = request.color,
            microchipId = request.microchipId,
            allergies = request.allergies,
            notes = request.notes,
            actorUserId = currentUser?.id,
            actorIp = ip
        )

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(patient.toResponse())
    }

    @Operation(summary = "Get a patient by ID")
    @GetMapping("/{id}")
    fun getPatient(
        @PathVariable id: UUID
    ): PatientResponse {
        val patient = patientService.getById(id)
        return patient.toResponse()
    }

    @Operation(summary = "Update a patient")
    @PutMapping("/{id}")
    fun updatePatient(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdatePatientRequest,
        httpServletRequest: HttpServletRequest
    ): PatientResponse {
        val currentUser = currentUserHolder.get()
        val ip = extractIp(httpServletRequest)

        val updated = patientService.updatePatient(
            id = id,
            name = request.name,
            species = request.species,
            breed = request.breed,
            sex = request.sex,
            dob = request.dob,
            color = request.color,
            microchipId = request.microchipId,
            allergies = request.allergies,
            notes = request.notes,
            actorUserId = currentUser?.id,
            actorIp = ip
        )

        return updated.toResponse()
    }

    @Operation(summary = "Delete a patient")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deletePatient(
        @PathVariable id: UUID,
        httpServletRequest: HttpServletRequest
    ) {
        val currentUser = currentUserHolder.get()
        val ip = extractIp(httpServletRequest)

        patientService.deletePatient(
            id = id,
            actorUserId = currentUser?.id,
            actorIp = ip
        )
    }

    @Operation(
        summary = "List or search patients",
        description = "List all patients, filter by ownerId, or search by name"
    )
    @GetMapping
    fun queryPatients(
        @RequestParam("ownerId", required = false) ownerId: UUID?,
        @RequestParam("name", required = false) name: String?
    ): List<PatientResponse> {
        val patients: List<Patient> = when {
            ownerId != null -> patientService.getByOwner(ownerId)
            !name.isNullOrBlank() -> patientService.searchByName(name)
            else -> patientService.listAll()
        }

        return patients.map { it.toResponse() }
    }

    @Operation(summary = "Find patient by microchip")
    @GetMapping("/by-microchip/{microchipId}")
    fun getByMicrochip(
        @PathVariable microchipId: String
    ): ResponseEntity<PatientResponse> {
        val patient = patientService.findByMicrochip(microchipId)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(patient.toResponse())
    }

    // --------- Helpers ---------

    private fun extractIp(request: HttpServletRequest): String? {
        val forwarded = request.getHeader("X-Forwarded-For")
        return forwarded?.split(",")?.firstOrNull()?.trim()
            ?: request.remoteAddr
    }
}

// DTOs & mapping unchanged from your current version
data class RegisterPatientRequest(
    val ownerId: UUID,

    @field:NotBlank
    @field:Size(max = 255)
    val name: String,

    @field:NotBlank
    @field:Size(max = 64)
    val species: String,

    @field:Size(max = 128)
    val breed: String? = null,

    @field:Size(max = 16)
    val sex: String? = null,
    val dob: LocalDate? = null,

    @field:Size(max = 64)
    val color: String? = null,

    @field:Size(max = 64)
    val microchipId: String? = null,
    val allergies: String? = null,
    val notes: String? = null
)

data class UpdatePatientRequest(
    val name: String? = null,
    val species: String? = null,
    val breed: String? = null,
    val sex: String? = null,
    val dob: LocalDate? = null,
    val color: String? = null,
    val microchipId: String? = null,
    val allergies: String? = null,
    val notes: String? = null
)

data class PatientResponse(
    val id: UUID,
    val ownerId: UUID,
    val name: String,
    val species: String,
    val breed: String?,
    val sex: String?,
    val dob: LocalDate?,
    val color: String?,
    val microchipId: String?,
    val allergies: String?,
    val notes: String?,
    val createdAt: String?,
    val updatedAt: String?
)

private fun Patient.toResponse(): PatientResponse =
    PatientResponse(
        id = this.id!!,
        ownerId = this.owner?.id!!,
        name = this.name,
        species = this.species,
        breed = this.breed,
        sex = this.sex,
        dob = this.dob,
        color = this.color,
        microchipId = this.microchipId,
        allergies = this.allergies,
        notes = this.notes,
        createdAt = this.createdAt.toString(),
        updatedAt = this.updatedAt.toString()
    )
