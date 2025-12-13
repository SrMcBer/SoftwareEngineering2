package com.vettrack.core.api.attachment

import org.springframework.core.io.Resource
import com.vettrack.core.auth.CurrentUserHolder
import com.vettrack.core.domain.Attachment
import com.vettrack.core.service.AttachmentService
import com.vettrack.core.storage.LocalAttachmentStorage
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID
import org.springframework.http.MediaType
import org.springframework.web.multipart.MultipartFile
import org.springframework.core.io.UrlResource
import org.springframework.http.HttpHeaders

@RestController
@RequestMapping("/api/v1")
class AttachmentController(
    private val currentUserHolder: CurrentUserHolder,
    private val attachmentService: AttachmentService,
    private val localAttachmentStorage: LocalAttachmentStorage
) {

    @PostMapping(
        "/patients/{patientId}/attachments",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    @ResponseStatus(HttpStatus.CREATED)
    fun uploadAttachment(
        @PathVariable patientId: UUID,
        @RequestParam type: String,
        @RequestParam(required = false) visitId: UUID?,
        @RequestParam(required = false) examId: UUID?,
        @RequestPart file: MultipartFile,
        request: HttpServletRequest
    ): AttachmentResponse {
        val currentUser = currentUserHolder.get()
        val actorIp = request.remoteAddr

        val attachment = attachmentService.uploadAttachmentMultipart(
            patientId = patientId,
            visitId = visitId,
            examId = examId,
            type = type,
            file = file,
            uploadedByUserId = currentUser?.id,
            actorIp = actorIp
        )

        return AttachmentResponse.from(attachment)
    }

    @GetMapping("/attachments/{attachmentId}/file")
    fun downloadAttachment(
        @PathVariable attachmentId: UUID
    ): ResponseEntity<Resource> {
        val attachment = attachmentService.getAttachment(attachmentId)

        val path = localAttachmentStorage.open(attachment.url)
        val resource = UrlResource(path.toUri())

        if (!resource.exists() || !resource.isReadable) {
            throw NoSuchElementException("File not found or not readable")
        }

        // Determine Content-Disposition based on type
        val disposition = if (attachment.type == "pdf" || attachment.type.startsWith("image")) {
            "inline" // Display in browser
        } else {
            "attachment" // Force download
        }

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(determineMediaType(attachment.type)))
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "$disposition; filename=\"${attachment.filename ?: "file"}\""
            )
            .body(resource)
    }

    @GetMapping("/patients/{patientId}/attachments")
    fun listPatientAttachments(
        @PathVariable patientId: UUID
    ): List<AttachmentResponse> {
        return attachmentService.listForPatient(patientId)
            .map { AttachmentResponse.from(it) }
    }

    @GetMapping("/visits/{visitId}/attachments")
    fun listVisitAttachments(
        @PathVariable visitId: UUID
    ): List<AttachmentResponse> {
        return attachmentService.listForVisit(visitId)
            .map { AttachmentResponse.from(it) }
    }

    @GetMapping("/exams/{examId}/attachments")
    fun listExamAttachments(
        @PathVariable examId: UUID
    ): List<AttachmentResponse> {
        return attachmentService.listForExam(examId)
            .map { AttachmentResponse.from(it) }
    }

    @DeleteMapping("/attachments/{attachmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteAttachment(
        @PathVariable attachmentId: UUID,
        request: HttpServletRequest
    ) {
        val actorIp = request.remoteAddr
        // TODO: Extract actual user ID from security context
        val userId: UUID? = null

        attachmentService.deleteAttachment(
            attachmentId = attachmentId,
            actorUserId = userId,
            actorIp = actorIp
        )
    }

    private fun determineMediaType(type: String): String {
        return when (type) {
            "image" -> "image/*"
            "pdf" -> "application/pdf"
            "video" -> "video/*"
            else -> "application/octet-stream"
        }
    }
}

// DTO for responses
data class AttachmentResponse(
    val id: UUID,
    val patientId: UUID,
    val visitId: UUID?,
    val examId: UUID?,
    val type: String,
    val filename: String?,
    val uploadedBy: UUID?,
    val uploadedAt: String,
    val downloadUrl: String
) {
    companion object {
        fun from(attachment: Attachment): AttachmentResponse {
            return AttachmentResponse(
                id = attachment.id!!,
                patientId = attachment.patient.id!!,
                visitId = attachment.visit?.id,
                examId = attachment.exam?.id,
                type = attachment.type,
                filename = attachment.filename,
                uploadedBy = attachment.uploadedBy?.id,
                uploadedAt = attachment.uploadedAt.toString(),
                downloadUrl = "/api/v1/attachments/${attachment.id}/file"
            )
        }
    }
}