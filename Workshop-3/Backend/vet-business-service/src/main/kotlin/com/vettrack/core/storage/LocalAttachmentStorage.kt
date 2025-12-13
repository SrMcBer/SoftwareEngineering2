package com.vettrack.core.storage

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.UUID

@Component
class LocalAttachmentStorage (
    @Value("\${vettrack.storage.attachments-dir}") private val rootDir: String
) {
    init {
        // Ensure the root directory exists on startup
        val root = Path.of(rootDir)
        if (!Files.exists(root)) {
            Files.createDirectories(root)
            println("Created attachments directory: $rootDir")
        }
    }

    fun save(patientId: UUID, attachmentId: UUID, file: MultipartFile): StoredFile {
        val original = sanitizeFilename(file.originalFilename ?: "file")
        val relativeKey = "$patientId/$attachmentId/$original"
        val targetPath: Path = Path.of(rootDir).resolve(relativeKey)

        Files.createDirectories(targetPath.parent)
        file.inputStream.use { input ->
            Files.copy(input, targetPath, StandardCopyOption.REPLACE_EXISTING)
        }

        return StoredFile(
            fileKey = relativeKey,
            absolutePath = targetPath,
            filename = original,
            mimeType = file.contentType,
            sizeBytes = file.size
        )
    }

    fun open(fileKey: String): Path {
        val path = Path.of(rootDir).resolve(fileKey).normalize()
        require(path.startsWith(Path.of(rootDir))) { "Invalid file path" }
        require(Files.exists(path)) { "File not found: $fileKey" }
        return path
    }

    fun delete(fileKey: String) {
        val path = open(fileKey)
        Files.deleteIfExists(path)

        // Optional: cleanup empty parent directories
        var parent = path.parent
        while (parent != null && parent.startsWith(Path.of(rootDir)) && parent != Path.of(rootDir)) {
            if (Files.list(parent).use { it.count() } == 0L) {
                Files.deleteIfExists(parent)
                parent = parent.parent
            } else {
                break
            }
        }
    }

    private fun sanitizeFilename(name: String): String =
        name.replace(Regex("[\\\\/]+"), "_")
            .replace(Regex("[^a-zA-Z0-9._-]"), "_")
            .replace(Regex("_+"), "_")
            .trim()
}

data class StoredFile(
    val fileKey: String,
    val absolutePath: Path,
    val filename: String,
    val mimeType: String?,
    val sizeBytes: Long
)