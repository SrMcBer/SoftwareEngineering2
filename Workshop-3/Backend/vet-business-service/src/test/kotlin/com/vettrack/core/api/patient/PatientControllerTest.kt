package com.vettrack.core.api.patient

import com.ninjasquad.springmockk.MockkBean
import com.vettrack.core.auth.CurrentUserHolder
import com.vettrack.core.domain.Owner
import com.vettrack.core.domain.Patient
import com.vettrack.core.service.PatientService
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

@WebMvcTest(PatientController::class)
class PatientControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var patientService: PatientService

    @MockkBean
    private lateinit var currentUserHolder: CurrentUserHolder

    private val testOwnerId = UUID.randomUUID()
    private val testPatientId = UUID.randomUUID()
    
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

    @Test
    fun `registerPatient_withValidRequest_returnsCreated`() {
        every { currentUserHolder.get() } returns null
        every { 
            patientService.registerPatient(
                ownerId = testOwnerId,
                name = "Fluffy",
                species = "Dog",
                breed = "Golden Retriever",
                sex = "Female",
                dob = LocalDate.of(2020, 5, 15),
                color = "Golden",
                microchipId = "123456789",
                allergies = "None",
                notes = "Friendly dog",
                actorUserId = null,
                actorIp = any()
            ) 
        } returns testPatient

        mockMvc.perform(
            post("/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "ownerId": "$testOwnerId",
                        "name": "Fluffy",
                        "species": "Dog",
                        "breed": "Golden Retriever",
                        "sex": "Female",
                        "dob": "2020-05-15",
                        "color": "Golden",
                        "microchipId": "123456789",
                        "allergies": "None",
                        "notes": "Friendly dog"
                    }
                """.trimIndent())
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(testPatientId.toString()))
            .andExpect(jsonPath("$.name").value("Fluffy"))
            .andExpect(jsonPath("$.species").value("Dog"))
            .andExpect(jsonPath("$.breed").value("Golden Retriever"))

        verify { patientService.registerPatient(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `registerPatient_withMissingRequiredFields_returnsBadRequest`() {
        mockMvc.perform(
            post("/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "ownerId": "$testOwnerId"
                    }
                """.trimIndent())
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `getPatient_withValidId_returnsPatient`() {
        every { patientService.getById(testPatientId) } returns testPatient

        mockMvc.perform(get("/patients/$testPatientId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(testPatientId.toString()))
            .andExpect(jsonPath("$.name").value("Fluffy"))
            .andExpect(jsonPath("$.species").value("Dog"))
    }

    @Test
    fun `updatePatient_withValidRequest_returnsUpdatedPatient`() {
        val updatedPatient = Patient(
            owner = testOwner,
            name = "Fluffy Jr",
            species = "Dog",
            breed = "Golden Retriever",
            sex = "Female",
            dob = LocalDate.of(2020, 5, 15),
            color = "Golden",
            microchipId = "123456789",
            allergies = "None",
            notes = "Friendly dog",
            createdAt = testPatient.createdAt,
            updatedAt = OffsetDateTime.now()
        ).apply { id = testPatientId }
        
        every { currentUserHolder.get() } returns null
        every { 
            patientService.updatePatient(
                id = testPatientId,
                name = "Fluffy Jr",
                species = null,
                breed = null,
                sex = null,
                dob = null,
                color = null,
                microchipId = null,
                allergies = null,
                notes = null,
                actorUserId = null,
                actorIp = any()
            ) 
        } returns updatedPatient

        mockMvc.perform(
            put("/patients/$testPatientId")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "Fluffy Jr"
                    }
                """.trimIndent())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Fluffy Jr"))

        verify { patientService.updatePatient(testPatientId, "Fluffy Jr", null, null, null, null, null, null, null, null, null, any()) }
    }

    @Test
    fun `deletePatient_withValidId_returnsNoContent`() {
        every { currentUserHolder.get() } returns null
        every { patientService.deletePatient(testPatientId, null, any()) } returns Unit

        mockMvc.perform(delete("/patients/$testPatientId"))
            .andExpect(status().isNoContent)

        verify { patientService.deletePatient(testPatientId, null, any()) }
    }

    @Test
    fun `queryPatients_withOwnerId_returnsPatientsForOwner`() {
        val patients = listOf(testPatient)
        every { patientService.getByOwner(testOwnerId) } returns patients

        mockMvc.perform(get("/patients?ownerId=$testOwnerId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(testPatientId.toString()))
            .andExpect(jsonPath("$[0].ownerId").value(testOwnerId.toString()))

        verify { patientService.getByOwner(testOwnerId) }
    }

    @Test
    fun `queryPatients_withName_returnsMatchingPatients`() {
        val patients = listOf(testPatient)
        every { patientService.searchByName("Fluffy") } returns patients

        mockMvc.perform(get("/patients?name=Fluffy"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].name").value("Fluffy"))

        verify { patientService.searchByName("Fluffy") }
    }

    @Test
    fun `queryPatients_withoutParams_returnsAllPatients`() {
        val patients = listOf(testPatient)
        every { patientService.listAll() } returns patients

        mockMvc.perform(get("/patients"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].name").value("Fluffy"))

        verify { patientService.listAll() }
    }

    @Test
    fun `getByMicrochip_whenFound_returnsPatient`() {
        every { patientService.findByMicrochip("123456789") } returns testPatient

        mockMvc.perform(get("/patients/by-microchip/123456789"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.microchipId").value("123456789"))

        verify { patientService.findByMicrochip("123456789") }
    }

    @Test
    fun `getByMicrochip_whenNotFound_returnsNotFound`() {
        every { patientService.findByMicrochip("999999999") } returns null

        mockMvc.perform(get("/patients/by-microchip/999999999"))
            .andExpect(status().isNotFound)

        verify { patientService.findByMicrochip("999999999") }
    }
}