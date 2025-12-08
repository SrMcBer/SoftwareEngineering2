package com.vettrack.core.api.owner

import com.vettrack.core.auth.CurrentUserHolder
import com.vettrack.core.service.OwnerService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import jakarta.validation.constraints.Email
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@Tag(name = "Owners", description = "Operations related to patient owners")
@RestController
@RequestMapping("/owners")
class OwnerController(
    private val ownerService: OwnerService,
    private val currentUserHolder: CurrentUserHolder
) {

    @Operation(summary = "Create a new owner")
    @PostMapping
    fun createOwner(
        @Valid @RequestBody request: CreateOwnerRequest,
        httpServletRequest: HttpServletRequest
    ): ResponseEntity<OwnerResponse> {
        val currentUser = currentUserHolder.get()
        val owner = ownerService.createOwner(
            name = request.name,
            phone = request.phone,
            email = request.email,
            actorUserId = currentUser?.id,
            actorIp = extractIp(httpServletRequest)
        )

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(owner.toResponse())
    }

    @Operation(summary = "Get an owner by ID")
    @GetMapping("/{id}")
    fun getOwner(
        @PathVariable id: UUID
    ): OwnerResponse {
        val owner = ownerService.getOwner(id)
        return owner.toResponse()
    }

    @Operation(summary = "Update an owner")
    @PutMapping("/{id}")
    fun updateOwner(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateOwnerRequest,
        httpServletRequest: HttpServletRequest
    ): OwnerResponse {
        val currentUser = currentUserHolder.get()
        val actorIp = extractIp(httpServletRequest)

        val updated = ownerService.updateOwner(
            id = id,
            name = request.name,
            phone = request.phone,
            email = request.email,
            actorUserId = currentUser?.id,
            actorIp = actorIp
        )

        return updated.toResponse()
    }

    @Operation(
        summary = "Delete an owner",
        description = "Will fail if the owner still has patients."
    )
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun deleteOwner(
        @PathVariable id: UUID,
        httpServletRequest: HttpServletRequest
    ) {
        val currentUser = currentUserHolder.get()
        val actorIp = extractIp(httpServletRequest)

        ownerService.deleteOwner(
            id = id,
            actorUserId = currentUser?.id,
            actorIp = actorIp
        )
    }

    @Operation(
        summary = "List or search owners",
        description = "List all owners or search by name (contains, case-insensitive)"
    )
    @GetMapping
    fun listOwners(
        @RequestParam("name", required = false) query: String?
    ): List<OwnerResponse> {
        val owners = if (!query.isNullOrBlank()) {
            ownerService.searchOwnersByName(query)
        } else {
            ownerService.listAllOwners()
        }

        return owners.map { it.toResponse() }
    }

    private fun extractIp(request: HttpServletRequest): String? {
        val forwarded = request.getHeader("X-Forwarded-For")
        return forwarded?.split(",")?.firstOrNull()?.trim()
            ?: request.remoteAddr
    }
}

// DTOs & mapping unchanged
data class CreateOwnerRequest(
    @field:NotBlank
    @field:Size(max = 255)
    val name: String,

    @field:Size(max = 32)
    val phone: String? = null,

    @field:Email
    @field:Size(max = 255)
    val email: String? = null
)

data class UpdateOwnerRequest(
    @field:Size(max = 255)
    val name: String? = null,

    @field:Size(max = 32)
    val phone: String? = null,

    @field:Email
    @field:Size(max = 255)
    val email: String? = null
)

data class OwnerResponse(
    val id: UUID,
    val name: String,
    val phone: String?,
    val email: String?,
    val createdAt: String?,
    val updatedAt: String?
)
