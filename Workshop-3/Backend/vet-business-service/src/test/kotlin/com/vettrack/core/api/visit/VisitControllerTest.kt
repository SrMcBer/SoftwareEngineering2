package com.vettrack.core.api.visit

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.vettrack.core.auth.CurrentUserHolder
import com.vettrack.core.domain.*
import com.vettrack.core.service.AttachmentService
import com.vettrack.core.service.ExamService
import com.vettrack.core.service.MedicationService
import com.vettrack.core.service.VisitService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.OffsetDateTime
import java.util.*

@WebMvcTest(VisitController::class)
@AutoConfigureMockMvc(addFilters = false)
class VisitControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var visitService: VisitService

    @MockkBean
    private lateinit var currentUserHolder: CurrentUserHolder

    @MockkBean
    private lateinit var examService: ExamService

    @MockkBean
    private lateinit var medicationService: MedicationService

    @MockkBean
    private lateinit var attachmentService: AttachmentService

    @MockkBean
    @Suppress("UnusedPrivateProperty")
    private lateinit var authUserClient: com.vettrack.core.auth.AuthUserClient

    private val visitId = UUID.randomUUID()
    private val patientId = UUID.randomUUID()

    private fun visitStub(): Visit {
        val patient = mockk<Patient>()
        every { patient.id } returns patientId
        every { patient.name } returns "Firulais"
        every { patient.species } returns "Canine"
        every { patient.breed } returns "Mestizo"
        every { patient.sex } returns "M"
        every { patient.dob } returns null
        every { patient.color } returns "Brown"
        every { patient.owner } returns null

        val visit = mockk<Visit>(relaxed = true)
        every { visit.id } returns visitId
        every { visit.patient } returns patient
        every { visit.dateTime } returns OffsetDateTime.now()
        every { visit.reason } returns "Checkup"
        every { visit.vitalsJson } returns """{"weight":10.0}"""
        every { visit.examNotes } returns "ok"
        every { visit.diagnoses } returns "healthy"
        every { visit.procedures } returns null
        every { visit.recommendations } returns null
        every { visit.createdBy } returns null
        every { visit.createdAt } returns OffsetDateTime.now()
        every { visit.updatedAt } returns OffsetDateTime.now()
        return visit
    }

    @TestConfiguration
    class TestConfig {
        // This bean definition ensures that ObjectMapper is present in the limited context
        // and satisfies the VisitController's dependency.
        @Bean
        fun objectMapper(): ObjectMapper = ObjectMapper()
    }

    @Test
    fun createVisit_returnsCreated() {
        every { currentUserHolder.get() } returns null
        every {
            visitService.createVisit(
                patientId = patientId,
                reason = "Checkup",
                vitalsJson = any(),
                examNotes = null,
                diagnoses = null,
                procedures = null,
                recommendations = null,
                createdByUserId = null,
                actorUserId = null,
                actorIp = any()
            )
        } returns visitStub()

        val body = mapOf(
            "patientId" to patientId.toString(),
            "reason" to "Checkup",
            "weightKg" to 10.0
        )

        mockMvc.perform(
            post("/visits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(visitId.toString()))
            .andExpect(jsonPath("$.patientId").value(patientId.toString()))
            .andExpect(jsonPath("$.reason").value("Checkup"))

        verify { visitService.createVisit(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun getVisitById_returnsVisit() {
        every { visitService.getById(visitId) } returns visitStub()

        mockMvc.perform(get("/visits/$visitId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(visitId.toString()))
            .andExpect(jsonPath("$.patientId").value(patientId.toString()))
    }

    @Test
    fun getVisitDetails_returnsAggregatedData() {
        val visit = visitStub()

        val exam = mockk<Exam>()
        every { exam.id } returns UUID.randomUUID()
        every { exam.template } returns mockk {
            every { id } returns UUID.randomUUID()
            every { name } returns "General Exam"
        }
        every { exam.status } returns ExamStatus.DRAFT
        every { exam.performedAt } returns OffsetDateTime.now()
        every { exam.performedBy } returns null
        every { exam.vitalsJson } returns null
        every { exam.resultsJson } returns """{"field":"value"}"""
        every { exam.visit } returns visit
        every { exam.status } returns ExamStatus.DRAFT

        val med = mockk<Medication>()
        every { med.id } returns UUID.randomUUID()
        every { med.name } returns "Carprofen"
        every { med.dosage } returns "25 mg"
        every { med.route } returns "PO"
        every { med.frequency } returns "BID"
        every { med.startDate } returns null
        every { med.endDate } returns null
        every { med.lastAdministeredAt } returns null
        every { med.nextDueAt } returns null

        val attachment = mockk<Attachment>()
        every { attachment.id } returns UUID.randomUUID()
        every { attachment.type } returns "image"
        every { attachment.filename } returns "xray.png"
        every { attachment.url } returns "http://example/xray.png"
        every { attachment.uploadedAt } returns OffsetDateTime.now()

        every { visitService.getById(visitId) } returns visit
        every { examService.listForPatient(patientId) } returns listOf(exam)
        every { medicationService.listActiveForPatient(patientId) } returns listOf(med)
        every { attachmentService.listForVisit(visitId) } returns listOf(attachment)

        mockMvc.perform(get("/visits/$visitId/details"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.visit.id").value(visitId.toString()))
            .andExpect(jsonPath("$.patient.id").value(patientId.toString()))
            .andExpect(jsonPath("$.exams[0].templateName").value("General Exam"))
            .andExpect(jsonPath("$.medications[0].name").value("Carprofen"))
            .andExpect(jsonPath("$.attachments[0].filename").value("xray.png"))
    }
}
