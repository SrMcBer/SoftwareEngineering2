package com.vettrack.vetbusinessservice.api

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

data class HealthResponse(
    val status: String,
    val db: String,
    val dbDetails: String? = null,
    )

@RestController
class HealthController (
    private val jdbcTemplate: JdbcTemplate,
) {

    @GetMapping("/health")
    fun health(): HealthResponse {
        return try {
            jdbcTemplate.execute("SELECT 1")

            val appUserTable: String? = jdbcTemplate.queryForObject(
                "SELECT to_regclass('public.app_user')",
                String::class.java
            )

            if (appUserTable == null) {
                // DB is reachable but schema is not as expected
                HealthResponse(
                    status = "degraded",
                    db = "reachable_but_schema_missing",
                    dbDetails = "Table 'public.app_user' does not exist.",
                )
            } else {
                HealthResponse(
                    status = "ok",
                    db = "up",
                    dbDetails = "DB reachable and 'app_user' table exists.",
                )
            }
        } catch (ex: Exception) {
            HealthResponse(
                status = "error",
                db = "down",
                dbDetails = ex.message,
            )
        }
    }
}