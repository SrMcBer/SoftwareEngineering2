package com.vettrack.core.api.reminder

import com.vettrack.core.auth.CurrentUserHolder
import com.vettrack.core.domain.Reminder
import com.vettrack.core.service.ReminderService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.OffsetDateTime
import java.util.UUID

@Tag(name = "Reminders", description = "Operations related to patient reminders")
@RestController
@RequestMapping("/reminders")
class ReminderController(
    private val reminderService: ReminderService,
    private val currentUserHolder: CurrentUserHolder
) {

    @Operation(summary = "Schedule a new reminder")
    @PostMapping
    fun scheduleReminder(
        @Valid @RequestBody request: CreateReminderRequest,
        httpServletRequest: HttpServletRequest
    ): ResponseEntity<ReminderResponse> {
        val currentUser = currentUserHolder.get()
        val reminder = reminderService.scheduleReminder(
            patientId = request.patientId,
            title = request.title,
            dueAt = request.dueAt,
            createdByUserId = currentUser?.id,
            actorUserId = currentUser?.id,
            actorIp = extractIp(httpServletRequest)
        )

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(reminder.toResponse())
    }

    @Operation(summary = "Get reminders for a specific patient")
    @GetMapping("/patient/{patientId}")
    fun getRemindersForPatient(
        @PathVariable patientId: UUID
    ): List<ReminderResponse> {
        val reminders = reminderService.listForPatient(patientId)
        return reminders.map { it.toResponse() }
    }

    @Operation(
        summary = "List overdue reminders",
        description = "Returns all pending reminders with due date before current time"
    )
    @GetMapping("/overdue")
    fun listOverdueReminders(): List<ReminderResponse> {
        val reminders = reminderService.listOverdue()
        return reminders.map { it.toResponse() }
    }

    @Operation(summary = "Mark a reminder as done")
    @PatchMapping("/{id}/done")
    fun markReminderDone(
        @PathVariable id: UUID,
        httpServletRequest: HttpServletRequest
    ): ReminderResponse {
        val currentUser = currentUserHolder.get()
        val reminder = reminderService.markDone(
            reminderId = id,
            actorUserId = currentUser?.id,
            actorIp = extractIp(httpServletRequest)
        )
        return reminder.toResponse()
    }

    @Operation(summary = "Dismiss a reminder")
    @PatchMapping("/{id}/dismiss")
    fun dismissReminder(
        @PathVariable id: UUID,
        httpServletRequest: HttpServletRequest
    ): ReminderResponse {
        val currentUser = currentUserHolder.get()
        val reminder = reminderService.dismissReminder(
            reminderId = id,
            actorUserId = currentUser?.id,
            actorIp = extractIp(httpServletRequest)
        )
        return reminder.toResponse()
    }

    private fun extractIp(request: HttpServletRequest): String? {
        val forwarded = request.getHeader("X-Forwarded-For")
        return forwarded?.split(",")?.firstOrNull()?.trim()
            ?: request.remoteAddr
    }
}

// DTOs
data class CreateReminderRequest(
    @field:NotNull
    var patientId: UUID,

    @field:NotBlank
    @field:Size(max = 255)
    val title: String,

    @field:NotNull
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    var dueAt: OffsetDateTime
)

data class ReminderResponse(
    val id: UUID?,
    val patientId: UUID,
    val patientName: String,
    val title: String,
    val dueAt: String,
    val status: String,
    val createdByUserId: UUID?,
    val createdByName: String?,
    val createdAt: String,
    val updatedAt: String
)

// Extension function for mapping
fun Reminder.toResponse() = ReminderResponse(
    id = this.id,
    patientId = this.patient.id!!,
    patientName = this.patient.name,
    title = this.title,
    dueAt = this.dueAt.toString(),
    status = this.status.name,
    createdByUserId = this.createdBy?.id,
    createdByName = this.createdBy?.name,
    createdAt = this.createdAt.toString(),
    updatedAt = this.updatedAt.toString()
)