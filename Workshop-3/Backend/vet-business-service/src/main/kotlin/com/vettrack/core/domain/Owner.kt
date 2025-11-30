package com.vettrack.core.domain

import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "owner")
class Owner (
    @Id
    @GeneratedValue
    @UuidGenerator
    var id: UUID? = null,

    @Column(nullable = false)
    var name: String,

    var phone: String? = null,

    var email: String? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now(),

    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY)
    var patients: MutableList<Patient> = mutableListOf()
) {
    fun shallowCopy(): Owner {
        return Owner(
            name = this.name,
            phone = this.phone,
            email = this.email,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        ).apply {
            id = this@Owner.id
        }
    }
}