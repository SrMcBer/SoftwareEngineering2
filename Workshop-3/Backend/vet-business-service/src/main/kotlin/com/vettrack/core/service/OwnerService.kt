package com.vettrack.core.service

import com.vettrack.core.domain.Owner
import com.vettrack.core.repository.OwnerRepository
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.UUID

@Service
class OwnerService(
    private val ownerRepository: OwnerRepository
){
    fun createOwner(
        name: String,
        phone: String? = null,
        email: String? = null
    ): Owner {
        val owner = Owner(
            name = name,
            phone = phone,
            email = email,
            createdAt = OffsetDateTime.now(),
            updatedAt = OffsetDateTime.now()
        )
        return ownerRepository.save(owner)
    }

    fun updateOwner(
        id: UUID,
        name: String? = null,
        phone: String? = null,
        email: String? = null
    ): Owner {
        val owner = ownerRepository.findById(id).orElseThrow {
            NoSuchElementException("Owner $id not found")
        }

        name?.let { owner.name = it }
        if (phone != null) owner.phone = phone
        if (email != null) owner.email = email
        owner.updatedAt = OffsetDateTime.now()

        return ownerRepository.save(owner)
    }

    fun findById(id: UUID): Owner =
        ownerRepository.findById(id).orElseThrow {
            NoSuchElementException("Owner $id not found")
        }

    fun searchByName(query: String): List<Owner> =
        ownerRepository.findByNameContainingIgnoreCase(query)
}