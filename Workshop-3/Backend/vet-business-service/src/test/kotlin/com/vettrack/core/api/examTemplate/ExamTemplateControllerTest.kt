package com.vettrack.core.api.examTemplate

import com.ninjasquad.springmockk.MockkBean
import com.vettrack.core.api.exam.ExamTemplateController
import com.vettrack.core.auth.AuthenticatedUser
import com.vettrack.core.auth.CurrentUserHolder
import com.vettrack.core.domain.ExamTemplate
import com.vettrack.core.domain.User
import com.vettrack.core.service.ExamTemplateService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.OffsetDateTime
import java.util.*

@WebMvcTest(ExamTemplateController::class)
@AutoConfigureMockMvc(addFilters = false)
class ExamTemplateControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var examTemplateService: ExamTemplateService

    @MockkBean
    private lateinit var currentUserHolder: CurrentUserHolder

    @MockkBean
    @Suppress("UnusedPrivateProperty")
    private lateinit var authUserClient: com.vettrack.core.auth.AuthUserClient

    private val templateId = UUID.randomUUID()
    private val userId = UUID.randomUUID()

    private fun templateStub(): ExamTemplate {
        val user = mockk<User>()
        every { user.id } returns userId

        val tpl = mockk<ExamTemplate>(relaxed = true)
        every { tpl.id } returns templateId
        every { tpl.name } returns "General Exam"
        every { tpl.description } returns "General template"
        every { tpl.fieldsJson } returns """{"field":"value"}"""
        every { tpl.isActive } returns true
        every { tpl.version } returns 1
        every { tpl.createdBy } returns user
        every { tpl.createdAt } returns OffsetDateTime.now()
        every { tpl.updatedAt } returns OffsetDateTime.now()
        return tpl
    }

    @Test
    fun createTemplate_returnsCreated() {
        val currentUser = mockk<AuthenticatedUser>(relaxed = true)
        every { currentUser.id } returns userId
        every { currentUserHolder.get() } returns currentUser

        every {
            examTemplateService.createTemplate(
                name = "General Exam",
                description = "General template",
                fieldsJson = """{"field":"value"}""",
                createdByUserId = userId,
                version = 1
            )
        } returns templateStub()

        mockMvc.perform(
            post("/exam-templates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "name": "General Exam",
                      "description": "General template",
                      "fieldsJson": "{\"field\":\"value\"}"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(templateId.toString()))
            .andExpect(jsonPath("$.name").value("General Exam"))

        verify { examTemplateService.createTemplate(any(), any(), any(), any(), any()) }
    }

    @Test
    fun deactivateTemplate_returnsTemplate() {
        every { examTemplateService.deactivateTemplate(templateId) } returns templateStub()

        mockMvc.perform(delete("/exam-templates/$templateId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(templateId.toString()))
    }

    @Test
    fun getActiveTemplates_returnsList() {
        every { examTemplateService.getActiveTemplates() } returns listOf(templateStub())

        mockMvc.perform(get("/exam-templates/active"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(templateId.toString()))
    }

    @Test
    fun getById_returnsTemplate() {
        every { examTemplateService.getById(templateId) } returns templateStub()

        mockMvc.perform(get("/exam-templates/$templateId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(templateId.toString()))
    }
}
