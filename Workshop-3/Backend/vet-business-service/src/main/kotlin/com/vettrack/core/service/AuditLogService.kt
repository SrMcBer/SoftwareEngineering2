package com.vettrack.core.service

import com.vettrack.core.domain.AuditLog
import com.vettrack.core.domain.User
import com.vettrack.core.repository.AuditLogRepository
import com.vettrack.core.repository.UserRepository
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.UUID

@Service
class AuditLogService(
    private val auditLogRepository: AuditLogRepository,
    private val userRepository: UserRepository
) {

    fun log(
        actorUserId: UUID?,
        entityType: String,
        entityId: UUID,
        action: String,
        diffSnapshotJson: String?,
        ip: String?
    ): AuditLog {
        val actor: User? = actorUserId?.let { uid ->
            userRepository.findById(uid).orElseThrow {
                NoSuchElementException("User $uid not found")
            }
        }

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
