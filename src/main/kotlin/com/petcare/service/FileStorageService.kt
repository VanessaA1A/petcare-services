package com.petcare.service

/*
 * Comentario de modulo PetCare:
 * Servicio de negocio. Contiene reglas de PetCare que no deben vivir directamente en los controladores.
 */

import com.petcare.exception.StorageException
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Instant

@Service
class FileStorageService {

    private val baseDir: Path = Paths.get("uploads/perfiles").toAbsolutePath().normalize()
    private val maxFileSize: Long = 5 * 1024 * 1024
    private val allowedContentTypes = setOf("image/jpeg", "image/png", "image/gif", "image/webp")

    init {
        try {
            Files.createDirectories(baseDir)
        } catch (ex: IOException) {
            throw StorageException("Could not create storage directory")
        }
    }

    fun storeProfileImage(userId: Int, file: MultipartFile): String {
        if (file.isEmpty) {
            throw StorageException("File is empty")
        }
        if (file.size > maxFileSize) {
            throw StorageException("File size exceeds maximum allowed 5MB")
        }
        if (!allowedContentTypes.contains(file.contentType)) {
            throw StorageException("File type not allowed")
        }

        val extension = when (file.contentType) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            "image/gif" -> "gif"
            "image/webp" -> "webp"
            else -> throw StorageException("Unsupported image type")
        }

        val filename = "foto_usuario_${userId}_${Instant.now().toEpochMilli()}.$extension"
        val targetLocation = baseDir.resolve(StringUtils.cleanPath(filename))
        if (!targetLocation.normalize().startsWith(baseDir)) {
            throw StorageException("Cannot store file outside the permitted directory")
        }

        try {
            Files.copy(file.inputStream, targetLocation)
        } catch (ex: IOException) {
            throw StorageException("Could not store file: ${ex.message}")
        }

        return filename
    }

    fun deleteProfileImage(filename: String?) {
        if (filename.isNullOrBlank()) return
        try {
            val target = baseDir.resolve(StringUtils.cleanPath(filename)).normalize()
            if (target.startsWith(baseDir) && Files.exists(target)) {
                Files.delete(target)
            }
        } catch (ex: IOException) {
            throw StorageException("Could not delete file: ${ex.message}")
        }
    }

    fun loadProfileImage(filename: String): Path {
        val cleaned = StringUtils.cleanPath(filename)
        val target = baseDir.resolve(cleaned).normalize()
        if (!target.startsWith(baseDir)) {
            throw StorageException("Cannot read file outside permitted directory")
        }
        if (Files.notExists(target) || !Files.isReadable(target)) {
            throw StorageException("File not found")
        }
        return target
    }
}
