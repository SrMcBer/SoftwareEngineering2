package com.vettrack.core.domain

import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "audit_log")
class AuditLog(

    @Id
    @GeneratedValue
    @UuidGenerator
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id")
    var actor: User? = null,

    @Column(name = "entity_type", nullable = false)
    var entityType: String,    // e.g. "patient", "visit"

    @Column(name = "entity_id", nullable = false, columnDefinition = "uuid")
    var entityId: UUID,

    @Column(nullable = false)
    var action: String,        // "create", "update", "delete"

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "diff_snapshot")
    var diffSnapshot: String? = null,

    @Column(name = "occurred_at", nullable = false)
    var occurredAt: OffsetDateTime = OffsetDateTime.now(),

    @JdbcTypeCode(SqlTypes.INET)   // 👈 here
    @Column(name = "ip")
    var ip: String? = null,
)