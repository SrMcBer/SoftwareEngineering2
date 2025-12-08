package com.vettrack.core.service

import com.vettrack.core.domain.AuditLog
import com.vettrack.core.repository.AuditLogRepository
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.UUID

@Service
class AuditLogService(
    private val auditLogRepository: AuditLogRepository,
    private val userService: UserService
) {

    fun log(
        actorUserId: UUID?,
        entityType: String,
        entityId: UUID,
        action: String,
        diffSnapshotJson: String?,
        ip: String?
    ): AuditLog {
        val actor = actorUserId?.let { userService.getById(it) }

        val log = AuditLog(
            actor = actor,
            entityType = entityType,
            entityId = entityId,
            action = action,
            diffSnapshot = diffSnapshotJson,
            occurredAt = OffsetDateTime.now(),
            ip = ip
        )

        return auditLogRepository.save(log)
    }

    fun getHistoryForEntity(entityType: String, entityId: UUID): List<AuditLog> =
        auditLogRepository.findByEntityTypeAndEntityIdOrderByOccurredAtDesc(entityType, entityId)

    fun getHistoryForUser(actorUserId: UUID): List<AuditLog> =
        auditLogRepository.findByActorIdOrderByOccurredAtDesc(actorUserId)
}