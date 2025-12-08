package com.vettrack.core.auth

import java.util.UUID

data class RemoteUserResponse(
    val id: UUID,
    val email: String,
    val name: String?,
    val role: String?,
)

data class AuthenticatedUser(
    val id: UUID,
    val email: String,
    val name: String?,
    val role: String?,
)