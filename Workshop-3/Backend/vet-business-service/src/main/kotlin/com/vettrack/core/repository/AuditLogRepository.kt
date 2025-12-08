package com.vettrack.core.repository

import com.vettrack.core.domain.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface AuditLogRepository : JpaRepository<AuditLog, UUID> {

    fun findByEntityTypeAndEntityIdOrderByOccurredAtDesc(
        entityType: String,
        entityId: UUID
    ): List<AuditLog>

    fun findByActorIdOrderByOccurredAtDesc(actorId: UUID): List<AuditLog>
}