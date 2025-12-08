package com.vettrack.core.api.exam

import com.ninjasquad.springmockk.MockkBean
import com.vettrack.core.auth.AuthenticatedUser
import com.vettrack.core.auth.CurrentUserHolder
import com.vettrack.core.domain.*
import com.vettrack.core.service.ExamService
import io.mockk.every
import io.mockk.mockk
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

@WebMvcTest(ExamController::class)
@AutoConfigureMockMvc(addFilters = false)
class ExamControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var examService: ExamService

    @MockkBean
    private lateinit var currentUserHolder: CurrentUserHolder

    @MockkBean
    private lateinit var authUserClient: com.vettrack.core.auth.AuthUserClient

    private val examId = UUID.randomUUID()
    private val patientId = UUID.randomUUID()
    private val visitId = UUID.randomUUID()
    private val templateId = UUID.randomUUID()
    private val userId = UUID.randomUUID()

    private fun examStub(): Exam {
        val patient = mockk<Patient>()
        every { patient.id } returns patientId

        val visit = mockk<Visit>()
        every { visit.id } returns visitId

        val template = mockk<ExamTemplate>()
        every { template.id } returns templateId

        val user = mockk<User>()
        every { user.id } returns userId

        val exam = mockk<Exam>(relaxed = true)
        every { exam.id } returns examId
        every { exam.patient } returns patient
        every { exam.visit } returns visit
        every { exam.template } returns template
        every { exam.status } returns ExamStatus.DRAFT
        every { exam.vitalsJson } returns """{"hr": 60}"""
        every { exam.resultsJson } returns """{"field":"value"}"""
        every { exam.performedBy } returns user
        every { exam.createdAt } returns OffsetDateTime.now()
        every { exam.updatedAt } returns OffsetDateTime.now()
        return exam
    }

    @Test
    fun createExamFromTemplate_returnsCreated() {
        val currentUser = mockk<AuthenticatedUser>(relaxed = true)
        every { currentUser.id } returns userId
        every { currentUserHolder.get() } returns currentUser

        every {
            examService.createExamFromTemplate(
                patientId = patientId,
                visitId = visitId,
                templateId = templateId,
                performedByUserId = userId,
                vitalsJson = """{"hr":60}""",
                resultsJson = """{"field":"value"}""",
                status = ExamStatus.DRAFT,
                actorUserId = userId,
                actorIp = any()
            )
        } returns examStub()

        mockMvc.perform(
            post("/exams/from-template")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "patientId": "$patientId",
                      "visitId": "$visitId",
                      "templateId": "$templateId",
                      "vitalsJson": "{\"hr\":60}",
                      "resultsJson": "{\"field\":\"value\"}"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(examId.toString()))
            .andExpect(jsonPath("$.patientId").value(patientId.toString()))
            .andExpect(jsonPath("$.visitId").value(visitId.toString()))
            .andExpect(jsonPath("$.templateId").value(templateId.toString()))
    }

    @Test
    fun updateExam_returnsUpdated() {
        val currentUser = mockk<AuthenticatedUser>(relaxed = true)
        every { currentUser.id } returns userId
        every { currentUserHolder.get() } returns currentUser

        val updated = examStub().also {
            every { it.resultsJson } returns """{"field":"updated"}"""
        }

        every {
            examService.updateExam(
                examId = examId,
                vitalsJson = null,
                resultsJson = """{"field":"updated"}""",
                actorUserId = userId,
                actorIp = any()
            )
        } returns updated

        mockMvc.perform(
            put("/exams/$examId")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{ "resultsJson": "{\"field\":\"updated\"}" }""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultsJson").value("""{"field":"updated"}"""))
    }

    @Test
    fun finalizeExam_returnsExam() {
        val currentUser = mockk<AuthenticatedUser>(relaxed = true)
        every { currentUser.id } returns userId
        every { currentUserHolder.get() } returns currentUser

        val finalized = examStub().also {
            every { it.status } returns ExamStatus.FINAL
        }

        every {
            examService.finalizeExam(
                examId = examId,
                actorUserId = userId,
                actorIp = any()
            )
        } returns finalized

        mockMvc.perform(post("/exams/$examId/finalize"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("FINAL"))
    }

    @Test
    fun getExamById_returnsExam() {
        every { examService.getExamById(examId) } returns examStub()

        mockMvc.perform(get("/exams/$examId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(examId.toString()))
    }

    @Test
    fun listForPatient_returnsList() {
        every { examService.listForPatient(patientId) } returns listOf(examStub())

        mockMvc.perform(get("/exams/patient/$patientId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(examId.toString()))
    }
}
