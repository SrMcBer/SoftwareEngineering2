package com.vettrack.core.api.owner

import com.vettrack.core.auth.CurrentUserHolder
import com.vettrack.core.service.OwnerService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import jakarta.validation.constraints.Email
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.util.UUID

@RestController
@RequestMapping("/owners")
class OwnerController (
    private val ownerService: OwnerService,
    private val currentUserHolder: CurrentUserHolder

) {
    /**
     * Create a new owner
     *
     * POST /owners
     */
    @PostMapping
    fun createOwner(
        @Valid @RequestBody request: CreateOwnerRequest,
        httpServletRequest: HttpServletRequest,
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

    /**
     * Get a single owner by id
     *
     * GET /owners/{id}
     */
    @GetMapping("/{id}")
    fun getOwner(
        @PathVariable id: UUID
    ): OwnerResponse {
        val owner = ownerService.getOwner(id)
        return owner.toResponse()
    }

    /**
     * Update an owner (full or partial)
     *
     * PUT /owners/{id}
     */
    @PutMapping("/{id}")
    fun updateOwner(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateOwnerRequest,
        httpServletRequest: HttpServletRequest,
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

    /**
     * Delete an owner
     *
     * DELETE /owners/{id}
     *
     * Will throw IllegalStateException from service if owner still has patients.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun deleteOwner(
        @PathVariable id: UUID,
        httpServletRequest: HttpServletRequest,
    ) {
        val currentUser = currentUserHolder.get()

        val actorIp = extractIp(httpServletRequest)

        ownerService.deleteOwner(
            id = id,
            actorUserId = currentUser?.id,
            actorIp = actorIp
        )
    }

    /**
     * List/search owners
     *
     * GET /owners
     *   - /owners            -> list all
     *   - /owners?name=foo      -> search by name (contains, case-insensitive)
     */
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

    // --------- Helpers for audit fields ---------

    /**
     * Very simple example:
     * - if Principal.name is a UUID string, we use it
     * - otherwise we just return null
     *
     * In a "real" setup, you'd adapt this to your JWT/custom principal.
     */
    private fun extractActorUserId(principal: Principal?): UUID? {
        val name = principal?.name ?: return null
        return try {
            UUID.fromString(name)
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    private fun extractIp(request: HttpServletRequest): String? {
        // If you have a proxy/load balancer, prefer X-Forwarded-For, etc.
        val forwarded = request.getHeader("X-Forwarded-For")
        return forwarded?.split(",")?.firstOrNull()?.trim()
            ?: request.remoteAddr
    }
}

// --------- DTOs ---------

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