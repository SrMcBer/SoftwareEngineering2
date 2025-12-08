package com.vettrack.core.api.owner

import com.ninjasquad.springmockk.MockkBean
import com.vettrack.core.auth.CurrentUserHolder
import com.vettrack.core.domain.Owner
import com.vettrack.core.service.OwnerService
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.OffsetDateTime
import java.util.*
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc

@WebMvcTest(OwnerController::class)
@AutoConfigureMockMvc(addFilters = false)
class OwnerControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var ownerService: OwnerService

    @MockkBean
    private lateinit var currentUserHolder: CurrentUserHolder

    @MockkBean
    @Suppress("UnusedPrivateProperty")
    private lateinit var authUserClient: com.vettrack.core.auth.AuthUserClient

    private val testOwnerId = UUID.randomUUID()
    private val testOwner = Owner(
        id = testOwnerId,
        name = "John Doe",
        phone = "1234567890",
        email = "john@example.com",
        createdAt = OffsetDateTime.now(),
        updatedAt = OffsetDateTime.now()
    )

    @Test
    fun createOwner_withValidRequest_returnsCreated() {
        every { currentUserHolder.get() } returns null
        every {
            ownerService.createOwner(
                name = "John Doe",
                phone = "1234567890",
                email = "john@example.com",
                actorUserId = null,
                actorIp = any()
            )
        } returns testOwner

        mockMvc.perform(
            post("/owners")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "name": "John Doe",
                        "phone": "1234567890",
                        "email": "john@example.com"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(testOwnerId.toString()))
            .andExpect(jsonPath("$.name").value("John Doe"))
            .andExpect(jsonPath("$.phone").value("1234567890"))
            .andExpect(jsonPath("$.email").value("john@example.com"))

        verify {
            ownerService.createOwner(
                name = "John Doe",
                phone = "1234567890",
                email = "john@example.com",
                actorUserId = null,
                actorIp = any()
            )
        }
    }

    @Test
    fun createOwner_withInvalidEmail_returnsBadRequest() {
        mockMvc.perform(
            post("/owners")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "name": "John Doe",
                        "email": "invalid-email"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun getOwner_withValidId_returnsOwner() {
        every { ownerService.getOwner(testOwnerId) } returns testOwner

        mockMvc.perform(get("/owners/$testOwnerId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(testOwnerId.toString()))
            .andExpect(jsonPath("$.name").value("John Doe"))
            .andExpect(jsonPath("$.email").value("john@example.com"))
    }

    @Test
    fun updateOwner_withValidRequest_returnsUpdatedOwner() {
        val updatedOwner = Owner(
            id = testOwnerId,
            name = "Jane Doe",
            phone = testOwner.phone,
            email = testOwner.email,
            createdAt = testOwner.createdAt,
            updatedAt = OffsetDateTime.now()
        )

        every { currentUserHolder.get() } returns null
        every {
            ownerService.updateOwner(
                id = testOwnerId,
                name = "Jane Doe",
                phone = null,
                email = null,
                actorUserId = null,
                actorIp = any()
            )
        } returns updatedOwner

        mockMvc.perform(
            put("/owners/$testOwnerId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "name": "Jane Doe"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(testOwnerId.toString()))
            .andExpect(jsonPath("$.name").value("Jane Doe"))

        verify {
            ownerService.updateOwner(
                id = testOwnerId,
                name = "Jane Doe",
                phone = null,
                email = null,
                actorUserId = null,
                actorIp = any()
            )
        }
    }

    @Test
    fun deleteOwner_withValidId_returnsAccepted() {
        every { currentUserHolder.get() } returns null
        every { ownerService.deleteOwner(testOwnerId, null, any()) } returns Unit

        mockMvc.perform(delete("/owners/$testOwnerId"))
            .andExpect(status().isAccepted)

        verify { ownerService.deleteOwner(testOwnerId, null, any()) }
    }

    @Test
    fun listOwners_withoutQuery_returnsAllOwners() {
        every { ownerService.listAllOwners() } returns listOf(testOwner)

        mockMvc.perform(get("/owners"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(testOwnerId.toString()))
            .andExpect(jsonPath("$[0].name").value("John Doe"))

        verify { ownerService.listAllOwners() }
    }

    @Test
    fun listOwners_withNameQuery_returnsFilteredOwners() {
        every { ownerService.searchOwnersByName("John") } returns listOf(testOwner)

        mockMvc.perform(get("/owners?name=John"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(testOwnerId.toString()))
            .andExpect(jsonPath("$[0].name").value("John Doe"))

        verify { ownerService.searchOwnersByName("John") }
    }
}