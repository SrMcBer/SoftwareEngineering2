package com.vettrack.core.api.visit

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.vettrack.core.auth.CurrentUserHolder
import com.vettrack.core.domain.Owner
import com.vettrack.core.domain.Patient
import com.vettrack.core.domain.Visit
import com.vettrack.core.service.VisitService
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*

@WebMvcTest(VisitController::class)
class VisitControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var visitService: VisitService

    @MockkBean
    private lateinit var currentUserHolder: CurrentUserHolder

    @MockkBean
    private lateinit var objectMapper: ObjectMapper

    private val testPatientId = UUID.randomUUID()
    private val testVisitId = UUID.randomUUID()
    private val testOwnerId = UUID.randomUUID()
    
    private val testOwner = Owner(
        id = testOwnerId,
        name = "John Doe",
        phone = "1234567890",
        email = "john@example.com",
        createdAt = OffsetDateTime.now(),
        updatedAt = OffsetDateTime.now()
    ).apply { id = testOwnerId }
    
    private val testPatient = Patient(
        owner = testOwner,
        name = "Fluffy",
        species = "Dog",
        breed = "Golden Retriever",
        sex = "Female",
        dob = LocalDate.of(2020, 5, 15),
        color = "Golden",
        microchipId = "123456789",
        allergies = "None",
        notes = "Friendly dog",
        createdAt = OffsetDateTime.now(),
        updatedAt = OffsetDateTime.now()
    ).apply { id = testPatientId }
    
    private val testVisit = Visit(
        patient = testPatient,
        dateTime = OffsetDateTime.now(),
        reason = "Annual checkup",
        vitalsJson = """{"weight":25.5,"heart_rate":80.0}""",
        examNotes = "Healthy dog",
        diagnoses = "None",
        procedures = "Vaccination",
        recommendations = "Continue current diet",
        createdBy = null,
        createdAt = OffsetDateTime.now(),
        updatedAt = OffsetDateTime.now()
    ).apply { id = testVisitId }

    @Test
    fun `createVisit_withValidRequest_returnsCreated`() {
        val vitalsJson = """{"weight":25.5,"heart_rate":80.0}"""
        
        every { currentUserHolder.get() } returns null
        every { objectMapper.writeValueAsString(any()) } returns vitalsJson
        every { 
            visitService.createVisit(
                patientId = testPatientId,
                reason = "Annual checkup",
                vitalsJson = vitalsJson,
                examNotes = "Healthy dog",
                diagnoses = "None",
                procedures = "Vaccination",
                recommendations = "Continue current diet",
                createdByUserId = null,
                actorUserId = null,
                actorIp = any()
            ) 
        } returns testVisit

        mockMvc.perform(
            post("/visits")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "patientId": "$testPatientId",
                        "reason": "Annual checkup",
                        "weightKg": 25.5,
                        "heartRate": 80.0,
                        "examNotes": "Healthy dog",
                        "diagnoses": "None",
                        "procedures": "Vaccination",
                        "recommendations": "Continue current diet"
                    }
                """.trimIndent())
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(testVisitId.toString()))
            .andExpect(jsonPath("$.reason").value("Annual checkup"))
            .andExpect(jsonPath("$.patientId").value(testPatientId.toString()))

        verify { visitService.createVisit(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `createVisit_withMissingRequiredFields_returnsBadRequest`() {
        mockMvc.perform(
            post("/visits")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "patientId": "$testPatientId"
                    }
                """.trimIndent())
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `getVisitById_withValidId_returnsVisit`() {
        every { visitService.getById(testVisitId) } returns testVisit

        mockMvc.perform(get("/visits/$testVisitId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(testVisitId.toString()))
            .andExpect(jsonPath("$.reason").value("Annual checkup"))
    }

    @Test
    fun `updateVisit_withValidRequest_returnsUpdatedVisit`() {
        val updatedVisit = Visit(
            patient = testPatient,
            dateTime = testVisit.dateTime,
            reason = "Follow-up",
            vitalsJson = testVisit.vitalsJson,
            examNotes = testVisit.examNotes,
            diagnoses = testVisit.diagnoses,
            procedures = testVisit.procedures,
            recommendations = testVisit.recommendations,
            createdBy = null,
            createdAt = testVisit.createdAt,
            updatedAt = OffsetDateTime.now()
        ).apply { id = testVisitId }
        
        every { currentUserHolder.get() } returns null
        every { objectMapper.writeValueAsString(any()) } returns null
        every { 
            visitService.updateVisit(
                visitId = testVisitId,
                reason = "Follow-up",
                vitalsJson = null,
                examNotes = null,
                diagnoses = null,
                procedures = null,
                recommendations = null,
                actorUserId = null,
                actorIp = any()
            ) 
        } returns updatedVisit

        mockMvc.perform(
            put("/visits/$testVisitId")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "reason": "Follow-up"
                    }
                """.trimIndent())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.reason").value("Follow-up"))

        verify { visitService.updateVisit(testVisitId, "Follow-up", null, null, null, null, null, null, any()) }
    }

    @Test
    fun `listVisitsForPatient_returnsVisits`() {
        val visits = listOf(testVisit)
        every { visitService.listForPatient(testPatientId) } returns visits

        mockMvc.perform(get("/visits/patient/$testPatientId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].patientId").value(testPatientId.toString()))

        verify { visitService.listForPatient(testPatientId) }
    }

    @Test
    fun `lastVisitForPatient_whenExists_returnsVisit`() {
        every { visitService.lastVisitForPatient(testPatientId) } returns testVisit

        mockMvc.perform(get("/visits/patient/$testPatientId/last"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(testVisitId.toString()))

        verify { visitService.lastVisitForPatient(testPatientId) }
    }

    @Test
    fun `lastVisitForPatient_whenNotExists_returnsNoContent`() {
        every { visitService.lastVisitForPatient(testPatientId) } returns null

        mockMvc.perform(get("/visits/patient/$testPatientId/last"))
            .andExpect(status().isNoContent)

        verify { visitService.lastVisitForPatient(testPatientId) }
    }

    @Test
    fun `listAllVisits_returnsAllVisits`() {
        val visits = listOf(testVisit)
        every { visitService.listAll() } returns visits

        mockMvc.perform(get("/visits"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(testVisitId.toString()))

        verify { visitService.listAll() }
    }

    @Test
    fun `listVisitsBetween_withDateRange_returnsFilteredVisits`() {
        val start = OffsetDateTime.now().minusDays(7)
        val end = OffsetDateTime.now()
        val visits = listOf(testVisit)
        
        every { visitService.listBetween(any(), any()) } returns visits

        mockMvc.perform(
            get("/visits/search")
                .param("start", start.toString())
                .param("end", end.toString())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(testVisitId.toString()))

        verify { visitService.listBetween(any(), any()) }
    }
}