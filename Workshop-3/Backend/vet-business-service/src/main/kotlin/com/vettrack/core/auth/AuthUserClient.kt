package com.vettrack.core.auth

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import java.util.UUID

@Service
class AuthUserClient (
    private val restClient: RestClient,
    @Value("http://localhost:8000/me")
    private val mePath: String
) {
    fun getCurrentUser(bearerToken: String): AuthenticatedUser? {
        return try {
            val response = restClient.get()
                .uri(mePath)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $bearerToken")
                .retrieve()
                .onStatus({ status -> status.is4xxClientError }) { _, _ ->
                    // 401 / 403 -> treat as unauthenticated
                    throw RuntimeException("Client error")
                }
                .body(RemoteUserResponse::class.java)

            response?.let {
                AuthenticatedUser(
                    id = it.id,
                    email = it.email,
                    name = it.name,
                    role = it.role
                )
            }
        } catch (ex: RestClientException) {
            // Log and treat as unauthenticated
            null
        }
    }
}