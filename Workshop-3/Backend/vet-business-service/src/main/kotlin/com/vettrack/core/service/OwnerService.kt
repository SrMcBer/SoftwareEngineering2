package com.vettrack.core.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.vettrack.core.domain.Owner
import com.vettrack.core.repository.OwnerRepository
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.UUID

@Service
class OwnerService(
    private val ownerRepository: OwnerRepository,
    private val patientOwnerQueryService: PatientOwnerQueryService,
    private val auditLogService: AuditLogService, // Updated dependency
    private val objectMapper: ObjectMapper
) {

    // --------- Public API (CRUD + search) ---------

    fun createOwner(
        name: String,
        phone: String?,
        email: String?,
        actorUserId: UUID?,
        actorIp: String?
    ): Owner {
        validateContactInfo(phone, email)

        val now = OffsetDateTime.now()
        val owner = Owner(
            name = name,
            phone = phone,
            email = email,
            createdAt = now,
            updatedAt = now
        )
        val saved = ownerRepository.save(owner)

        val diffJson = buildDiffJson(
            old = null,
            new = saved,
            action = "create"
        )

        // Updated to use the concrete service method .log()
        auditLogService.log(
            actorUserId = actorUserId,
            entityType = "owner",
            entityId = saved.id!!,
            action = "create",
            diffSnapshotJson = diffJson,
            ip = actorIp
        )

        return saved
    }

    fun getOwner(id: UUID): Owner =
        ownerRepository.findById(id).orElseThrow {
            NoSuchElementException("Owner $id not found")
        }


    fun updateOwner(
        id: UUID,
        name: String?,
        phone: String?,
        email: String?,
        actorUserId: UUID?,
        actorIp: String?
    ): Owner {
        val existing = ownerRepository.findById(id).orElseThrow {
            NoSuchElementException("Owner $id not found")
        }

        validateContactInfo(phone ?: existing.phone, email ?: existing.email)

        // Ensure your Owner entity has a shallowCopy or copy method for this
        val before = existing.shallowCopy()

        name?.let { existing.name = it }
        if (phone != null) existing.phone = phone
        if (email != null) existing.email = email
        existing.updatedAt = OffsetDateTime.now()

        val saved = ownerRepository.save(existing)

        val diffJson = buildDiffJson(
            old = before,
            new = saved,
            action = "update"
        )

        // Updated to use the concrete service method .log()
        auditLogService.log(
            actorUserId = actorUserId,
            entityType = "owner",
            entityId = saved.id!!,
            action = "update",
            diffSnapshotJson = diffJson,
            ip = actorIp
        )

        return saved
    }

    fun deleteOwner(
        id: UUID,
        actorUserId: UUID?,
        actorIp: String?
    ) {
        val owner = getOwner(id)

        // Optional: prevent hard delete if owner still has patients
        val hasPatients = patientOwnerQueryService.getByOwnerId(id).isNotEmpty()
        if (hasPatients) {
            throw IllegalStateException("Cannot delete owner $id because they still have registered patients")
        }

        ownerRepository.delete(owner)

        val diffJson = buildDiffJson(
            old = owner,
            new = null,
            action = "delete"
        )

        // Updated to use the concrete service method .log()
        auditLogService.log(
            actorUserId = actorUserId,
            entityType = "owner",
            entityId = id,
            action = "delete",
            diffSnapshotJson = diffJson,
            ip = actorIp
        )
    }

    fun searchOwnersByName(query: String): List<Owner> =
        ownerRepository.findByNameContainingIgnoreCase(query)

    fun listAllOwners(): List<Owner> =
        ownerRepository.findAll()

    // --------- Validation logic ---------

    private fun validateContactInfo(phone: String?, email: String?) {
        // Email: simple format check, nullable allowed
        if (!email.isNullOrBlank()) {
            val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
            require(emailRegex.matches(email)) {
                "Invalid email format"
            }
        }

        // Phone: optional, but if present check that it is mostly digits (+ allowed)
        if (!phone.isNullOrBlank()) {
            val phoneRegex = "^[0-9+()\\-\\s]{5,20}$".toRegex()
            require(phoneRegex.matches(phone)) {
                "Invalid phone format"
            }
        }
    }

    // --------- Diff builder for audit ---------

    private fun buildDiffJson(
        old: Owner?,
        new: Owner?,
        action: String
    ): String? {
        val diff = mutableMapOf<String, Any?>()
        diff["action"] = action

        if (old == null && new != null) {
            // full snapshot on create
            diff["new"] = mapOf(
                "id" to new.id,
                "name" to new.name,
                "phone" to new.phone,
                "email" to new.email
            )
        } else if (old != null && new == null) {
            // full snapshot on delete
            diff["old"] = mapOf(
                "id" to old.id,
                "name" to old.name,
                "phone" to old.phone,
                "email" to old.email
            )
        } else if (old != null && new != null) {
            val changes = mutableMapOf<String, Any?>()

            if (old.name != new.name) {
                changes["name"] = mapOf("old" to old.name, "new" to new.name)
            }
            if (old.phone != new.phone) {
                changes["phone"] = mapOf("old" to old.phone, "new" to new.phone)
            }
            if (old.email != new.email) {
                changes["email"] = mapOf("old" to old.email, "new" to new.email)
            }

            if (changes.isNotEmpty()) {
                diff["changes"] = changes
            }
        }

        return if (diff.isEmpty()) null else objectMapper.writeValueAsString(diff)
    }
}