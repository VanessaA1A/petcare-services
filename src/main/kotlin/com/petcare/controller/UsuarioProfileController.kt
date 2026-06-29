package com.petcare.controller

import com.petcare.dto.UpdateProfileRequest
import com.petcare.dto.UserProfileResponse
import com.petcare.exception.StorageException
import com.petcare.exception.UserNotFoundException
import com.petcare.model.User
import com.petcare.service.FileStorageService
import com.petcare.service.UserService
import com.petcare.util.RoleUtil
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.net.MalformedURLException
import java.nio.file.Path

@RestController
@RequestMapping("/api/usuarios")
class UsuarioProfileController(
    private val userService: UserService,
    private val fileStorageService: FileStorageService
) {

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    fun getAuthenticatedProfile(authentication: Authentication): ResponseEntity<UserProfileResponse> {
        val principal = authentication.principal as? com.petcare.security.UserPrincipal
            ?: throw UserNotFoundException("Authenticated user not found")
        val user = userService.findById(principal.id).orElseThrow { UserNotFoundException("User not found") }
        return ResponseEntity.ok(UserProfileResponse.fromUser(user))
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    fun updateProfile(authentication: Authentication, @RequestBody request: UpdateProfileRequest): ResponseEntity<UserProfileResponse> {
        val principal = authentication.principal as? com.petcare.security.UserPrincipal
            ?: throw UserNotFoundException("Authenticated user not found")
        val user = userService.findById(principal.id).orElseThrow { UserNotFoundException("User not found") }
        user.nombre = request.nombre ?: user.nombre
        user.apellido = request.apellido ?: user.apellido
        user.telefono = request.telefono ?: user.telefono
        val saved = userService.save(user)
        return ResponseEntity.ok(UserProfileResponse.fromUser(saved))
    }

    @PostMapping("/me/foto")
    @PreAuthorize("isAuthenticated()")
    fun uploadProfilePhoto(authentication: Authentication, @RequestParam("file") file: MultipartFile): ResponseEntity<UserProfileResponse> {
        val principal = authentication.principal as? com.petcare.security.UserPrincipal
            ?: throw UserNotFoundException("Authenticated user not found")
        val user = userService.findById(principal.id).orElseThrow { UserNotFoundException("User not found") }

        user.fotoPerfilFilename?.let { fileStorageService.deleteProfileImage(it) }
        val filename = fileStorageService.storeProfileImage(user.id!!, file)
        user.fotoPerfilFilename = filename
        user.fotoPerfilUrl = "/api/usuarios/${user.id}/foto"
        val saved = userService.save(user)
        return ResponseEntity.ok(UserProfileResponse.fromUser(saved))
    }

    @DeleteMapping("/me/foto")
    @PreAuthorize("isAuthenticated()")
    fun deleteProfilePhoto(authentication: Authentication): ResponseEntity<Any> {
        val principal = authentication.principal as? com.petcare.security.UserPrincipal
            ?: throw UserNotFoundException("Authenticated user not found")
        val user = userService.findById(principal.id).orElseThrow { UserNotFoundException("User not found") }
        fileStorageService.deleteProfileImage(user.fotoPerfilFilename)
        user.fotoPerfilFilename = null
        user.fotoPerfilUrl = null
        userService.save(user)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}/foto")
    fun serveProfilePhoto(@PathVariable id: Int): ResponseEntity<*> {
        val user = userService.findById(id).orElseThrow { UserNotFoundException("User not found") }
        val filename = user.fotoPerfilFilename ?: throw StorageException("User has no profile photo")
        val path: Path = fileStorageService.loadProfileImage(filename)
        val resource: Resource = try {
            UrlResource(path.toUri())
        } catch (ex: MalformedURLException) {
            throw StorageException("Could not read file: ${ex.message}")
        }
        val contentType = when (path.toString().substringAfterLast('.', "jpg").lowercase()) {
            "png" -> MediaType.IMAGE_PNG
            "gif" -> MediaType.IMAGE_GIF
            "webp" -> MediaType.valueOf("image/webp")
            else -> MediaType.IMAGE_JPEG
        }
        return ResponseEntity.ok()
            .contentType(contentType)
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"${path.fileName}\"")
            .body(resource)
    }
}
