package com.vettrack.core.auth

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class RemoteAuthFilter (
    private val authUserClient: AuthUserClient,
    private val currentUserHolder: CurrentUserHolder
) : OncePerRequestFilter() {
    private val log = LoggerFactory.getLogger(RemoteAuthFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val authHeader = request.getHeader("Authorization")

            if (authHeader != null && authHeader.startsWith("Bearer ", ignoreCase = true)) {
                val token = authHeader.removePrefix("Bearer ").trim()

                if (token.isNotBlank()) {
                    val user = authUserClient.getCurrentUser(token)
                    if (user != null) {
                        currentUserHolder.set(user)
                    } else {
                         response.status = HttpServletResponse.SC_UNAUTHORIZED
                         return
                    }
                }
            }

            filterChain.doFilter(request, response)
        } finally {
            // Make sure we don’t leak user between requests/threads
            currentUserHolder.clear()
        }
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI
        return path.startsWith("/actuator") ||
                path == "/health" ||
                path.startsWith("/docs") ||
                path.startsWith("/swagger")
    }
}