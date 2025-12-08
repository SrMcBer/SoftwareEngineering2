package com.vettrack.core.api

import com.ninjasquad.springmockk.MockkBean
import com.vettrack.core.auth.AuthUserClient
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.lang.RuntimeException
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc

@WebMvcTest(HealthController::class)
@AutoConfigureMockMvc(addFilters = false)
class HealthControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var jdbcTemplate: JdbcTemplate

    @MockkBean
    private lateinit var CurrentUserHolder: com.vettrack.core.auth.CurrentUserHolder

    @MockkBean
    private lateinit var AuthUserClient: com.vettrack.core.auth.AuthUserClient

    @Test
    fun health_okWhenDbAndTableExist() {
        every { jdbcTemplate.execute("SELECT 1") } returns Unit
        every {
            jdbcTemplate.queryForObject(
                "SELECT to_regclass('public.app_user')",
                String::class.java
            )
        } returns "app_user"

        mockMvc.perform(get("/health"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("ok"))
            .andExpect(jsonPath("$.db").value("up"))
    }

    @Test
    fun health_degradedWhenTableMissing() {
        every { jdbcTemplate.execute("SELECT 1") } returns Unit
        every {
            jdbcTemplate.queryForObject(
                "SELECT to_regclass('public.app_user')",
                String::class.java
            )
        } returns null

        mockMvc.perform(get("/health"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("degraded"))
            .andExpect(jsonPath("$.db").value("reachable_but_schema_missing"))
    }

    @Test
    fun health_errorWhenExceptionThrown() {
        every { jdbcTemplate.execute("SELECT 1") } throws RuntimeException("DB down")

        mockMvc.perform(get("/health"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("error"))
            .andExpect(jsonPath("$.db").value("down"))
    }
}
