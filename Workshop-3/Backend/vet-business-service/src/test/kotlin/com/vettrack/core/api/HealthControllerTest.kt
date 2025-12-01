package com.vettrack.core.api

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(HealthController::class)
class HealthControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var jdbcTemplate: JdbcTemplate

    @Test
    fun health_whenDbIsUp_returnsOk() {
        every { jdbcTemplate.execute(any<String>()) } returns Unit
        every { jdbcTemplate.queryForObject(any<String>(), String::class.java) } returns "public.app_user"

        mockMvc.perform(get("/health"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("ok"))
            .andExpect(jsonPath("$.db").value("up"))
    }

    @Test
    fun health_whenSchemaIsMissing_returnsDegraded() {
        every { jdbcTemplate.execute(any<String>()) } returns Unit
        every { jdbcTemplate.queryForObject(any<String>(), String::class.java) } returns null

        mockMvc.perform(get("/health"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("degraded"))
            .andExpect(jsonPath("$.db").value("reachable_but_schema_missing"))
    }

    @Test
    fun health_whenDbIsDown_returnsError() {
        every { jdbcTemplate.execute(any<String>()) } throws RuntimeException("Connection refused")

        mockMvc.perform(get("/health"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("error"))
            .andExpect(jsonPath("$.db").value("down"))
    }
}
