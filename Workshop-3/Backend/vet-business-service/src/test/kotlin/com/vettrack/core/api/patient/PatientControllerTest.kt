package com.vettrack.core.api.patient

import com.ninjasquad.springmockk.MockkBean
import com.vettrack.core.auth.CurrentUserHolder
import com.vettrack.core.domain.Owner
import com.vettrack.core.domain.Patient
import com.vettrack.core.service.PatientService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDate
import java.util.*
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc

@WebMvcTest(PatientController::class)
@AutoConfigureMockMvc(addFilters = false)
class PatientControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var patientService: PatientService

    @MockkBean
    private lateinit var currentUserHolder: CurrentUserHolder

    @MockkBean
    @Suppress("UnusedPrivateProperty")
    private lateinit var authUserClient: com.vettrack.core.auth.AuthUserClient

    private val ownerId = UUID.randomUUID()
    private val patientId = UUID.randomUUID()

    private fun patientStub(): Patient {
        val owner = mockk<Owner>()
        every { owner.id } returns ownerId

        val patient = mockk<Patient>(relaxed = true)
        every { patient.id } returns patientId
        every { patient.owner } returns owner
        every { patient.name } returns "Firulais"
        every { patient.species } returns "Canine"
        every { patient.breed } returns "Mestizo"
        every { patient.sex } returns "M"
        every { patient.dob } returns LocalDate.of(2020, 1, 1)
        every { patient.color } returns "Brown"
        every { patient.microchipId } returns "12345"
        every { patient.allergies } returns "None"
        every { patient.notes } returns "Healthy"
        every { patient.createdAt } returns java.time.OffsetDateTime.now()
        every { patient.updatedAt } returns java.time.OffsetDateTime.now()
        return patient
    }

    @Test
    fun registerPatient_returnsCreated() {
        every { currentUserHolder.get() } returns null
        every {
            patientService.registerPatient(
                ownerId = ownerId,
                name = "Firulais",
                species = "Canine",
                breed = "Mestizo",
                sex = "M",
                dob = LocalDate.of(2020, 1, 1),
                color = "Brown",
                microchipId = "12345",
                allergies = "None",
                notes = "Healthy",
                actorUserId = null,
                actorIp = any()
            )
        } returns patientStub()

        mockMvc.perform(
            post("/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "ownerId": "$ownerId",
                      "name": "Firulais",
                      "species": "Canine",
                      "breed": "Mestizo",
                      "sex": "M",
                      "dob": "2020-01-01",
                      "color": "Brown",
                      "microchipId": "12345",
                      "allergies": "None",
                      "notes": "Healthy"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(patientId.toString()))
            .andExpect(jsonPath("$.ownerId").value(ownerId.toString()))
            .andExpect(jsonPath("$.name").value("Firulais"))
            .andExpect(jsonPath("$.species").value("Canine"))

        verify { patientService.registerPatient(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun getPatient_returnsPatient() {
        every { patientService.getById(patientId) } returns patientStub()

        mockMvc.perform(get("/patients/$patientId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(patientId.toString()))
            .andExpect(jsonPath("$.name").value("Firulais"))
    }

    @Test
    fun updatePatient_returnsUpdated() {
        val updated = patientStub().also {
            every { it.name } returns "Firulais Jr"
        }

        every { currentUserHolder.get() } returns null
        every {
            patientService.updatePatient(
                id = patientId,
                name = "Firulais Jr",
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
        } returns updated

        mockMvc.perform(
            put("/patients/$patientId")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{ "name": "Firulais Jr" }""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Firulais Jr"))
    }

    @Test
    fun deletePatient_returnsNoContent() {
        every { currentUserHolder.get() } returns null
        every { patientService.deletePatient(patientId, null, any()) } returns Unit

        mockMvc.perform(delete("/patients/$patientId"))
            .andExpect(status().isNoContent)

        verify { patientService.deletePatient(patientId, null, any()) }
    }

    @Test
    fun queryPatients_byOwnerId() {
        every { patientService.getByOwner(ownerId) } returns listOf(patientStub())

        mockMvc.perform(get("/patients?ownerId=$ownerId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(patientId.toString()))
    }

    @Test
    fun getByMicrochip_found() {
        every { patientService.findByMicrochip("12345") } returns patientStub()

        mockMvc.perform(get("/patients/by-microchip/12345"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.microchipId").value("12345"))
    }

    @Test
    fun getByMicrochip_notFound() {
        every { patientService.findByMicrochip("999") } returns null

        mockMvc.perform(get("/patients/by-microchip/999"))
            .andExpect(status().isNotFound)
    }
}
