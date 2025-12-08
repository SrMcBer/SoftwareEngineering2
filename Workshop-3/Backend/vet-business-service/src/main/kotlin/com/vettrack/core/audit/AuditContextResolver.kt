package com.vettrack.core.audit

import com.vettrack.core.auth.CurrentUserHolder
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component
import java.util.UUID

data class AuditContext(
    val actorUserId: UUID?,
    val actorIp: String?
)

@Component
class AuditContextResolver(
    private val currentUserHolder: CurrentUserHolder
) {

    fun from(request: HttpServletRequest): AuditContext {
        val currentUser = currentUserHolder.get()
        return AuditContext(
            actorUserId = currentUser?.id,
            actorIp = extractIp(request)
        )
    }

    /**
     * For cases where you don’t have a HttpServletRequest
     * (scheduled jobs, internal calls, etc.).
     */
    fun fromSystem(systemUserId: UUID? = null): AuditContext =
        AuditContext(
            actorUserId = systemUserId,
            actorIp = null
        )

    private fun extractIp(request: HttpServletRequest): String? {
        val forwarded = request.getHeader("X-Forwarded-For")
        return forwarded
            ?.split(",")
            ?.firstOrNull()
            ?.trim()
            ?: request.remoteAddr
    }
}
