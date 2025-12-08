package com.vettrack.core.api.medication

import com.ninjasquad.springmockk.MockkBean
import com.vettrack.core.auth.AuthenticatedUser
import com.vettrack.core.auth.CurrentUserHolder
import com.vettrack.core.domain.DoseEvent
import com.vettrack.core.domain.Medication
import com.vettrack.core.domain.Patient
import com.vettrack.core.domain.User
import com.vettrack.core.service.MedicationService
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
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*

@WebMvcTest(MedicationController::class)
@AutoConfigureMockMvc(addFilters = false)
class MedicationControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var medicationService: MedicationService

    @MockkBean
    private lateinit var currentUserHolder: CurrentUserHolder

    @MockkBean
    @Suppress("UnusedPrivateProperty")
    private lateinit var authUserClient: com.vettrack.core.auth.AuthUserClient

    private val patientId = UUID.randomUUID()
    private val medicationId = UUID.randomUUID()
    private val doseId = UUID.randomUUID()
    private val userId = UUID.randomUUID()

    private fun medicationStub(): Medication {
        val patient = mockk<Patient>()
        every { patient.id } returns patientId

        val user = mockk<User>()
        every { user.id } returns userId

        val med = mockk<Medication>(relaxed = true)
        every { med.id } returns medicationId
        every { med.patient } returns patient
        every { med.name } returns "Carprofen"
        every { med.dosage } returns "25 mg"
        every { med.route } returns "PO"
        every { med.frequency } returns "BID"
        every { med.startDate } returns LocalDate.of(2025, 1, 1)
        every { med.endDate } returns null
        every { med.createdBy } returns user
        every { med.createdAt } returns OffsetDateTime.now()
        every { med.updatedAt } returns OffsetDateTime.now()
        every { med.isActive() } returns true
        return med
    }

    private fun doseStub(): DoseEvent {
        val med = medicationStub()
        val user = mockk<User>()
        every { user.id } returns userId

        val dose = mockk<DoseEvent>(relaxed = true)
        every { dose.id } returns doseId
        every { dose.medication } returns med
        every { dose.administeredAt } returns OffsetDateTime.now()
        every { dose.amount } returns "25 mg"
        every { dose.notes } returns "ok"
        every { dose.recordedBy } returns user
        return dose
    }

    @Test
    fun prescribeMedication_returnsCreated() {
        val currentUser = mockk<AuthenticatedUser>(relaxed = true)
        every { currentUser.id } returns userId
        every { currentUserHolder.get() } returns currentUser

        every {
            medicationService.prescribeMedication(
                patientId = patientId,
                name = "Carprofen",
                dosage = "25 mg",
                route = "PO",
                frequency = "BID",
                startDate = LocalDate.of(2025, 1, 1),
                endDate = null,
                createdByUserId = userId,
                actorUserId = userId,
                actorIp = any()
            )
        } returns medicationStub()

        mockMvc.perform(
            post("/medications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "patientId": "$patientId",
                      "name": "Carprofen",
                      "dosage": "25 mg",
                      "route": "PO",
                      "frequency": "BID",
                      "startDate": "2025-01-01"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(medicationId.toString()))
            .andExpect(jsonPath("$.patientId").value(patientId.toString()))
            .andExpect(jsonPath("$.name").value("Carprofen"))
    }

    @Test
    fun updateMedication_returnsUpdated() {
        val currentUser = mockk<AuthenticatedUser>(relaxed = true)
        every { currentUser.id } returns userId
        every { currentUserHolder.get() } returns currentUser

        val updated = medicationStub().also {
            every { it.dosage } returns "50 mg"
        }

        every {
            medicationService.updateMedication(
                medicationId = medicationId,
                dosage = "50 mg",
                route = null,
                frequency = null,
                startDate = null,
                endDate = null,
                actorUserId = userId,
                actorIp = any()
            )
        } returns updated

        mockMvc.perform(
            put("/medications/$medicationId")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{ "dosage": "50 mg" }""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.dosage").value("50 mg"))
    }

    @Test
    fun endMedication_setsEndDate() {
        val currentUser = mockk<AuthenticatedUser>(relaxed = true)
        every { currentUser.id } returns userId
        every { currentUserHolder.get() } returns currentUser

        val ended = medicationStub().also {
            every { it.endDate } returns LocalDate.of(2025, 2, 1)
        }

        every {
            medicationService.endMedication(
                medicationId = medicationId,
                endDate = LocalDate.of(2025, 2, 1),
                actorUserId = userId,
                actorIp = any()
            )
        } returns ended

        mockMvc.perform(
            post("/medications/$medicationId/end")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{ "endDate": "2025-02-01" }""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.endDate").value("2025-02-01"))
    }

    @Test
    fun recordDose_returnsCreated() {
        val currentUser = mockk<AuthenticatedUser>(relaxed = true)
        every { currentUser.id } returns userId
        every { currentUserHolder.get() } returns currentUser

        every {
            medicationService.recordDose(
                medicationId = medicationId,
                amount = "25 mg",
                notes = "ok",
                recordedByUserId = userId,
                actorIp = any()
            )
        } returns doseStub()

        mockMvc.perform(
            post("/medications/$medicationId/doses")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{ "amount": "25 mg", "notes": "ok" }""")
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(doseId.toString()))
            .andExpect(jsonPath("$.medicationId").value(medicationId.toString()))
    }

    @Test
    fun listActiveForPatient_returnsList() {
        every { medicationService.listActiveForPatient(patientId) } returns listOf(medicationStub())

        mockMvc.perform(get("/patients/$patientId/medications"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(medicationId.toString()))
            .andExpect(jsonPath("$[0].patientId").value(patientId.toString()))
    }
}
