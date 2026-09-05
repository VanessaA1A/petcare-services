package com.petcare.controller

/*
 * Comentario de modulo PetCare:
 * Controlador REST. Recibe peticiones HTTP, valida el flujo basico y delega la logica al servicio.
 */

import com.petcare.dto.UpdateProfileRequest
import com.petcare.dto.UserProfileResponse
import com.petcare.exception.StorageException
import com.petcare.exception.UserNotFoundException
import com.petcare.model.User
import com.petcare.service.FileStorageService
import com.petcare.service.NotificationService
import com.petcare.service.UserService
import com.petcare.util.RoleUtil
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
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
@Tag(name = "Perfil de usuario", description = "Perfil del usuario autenticado (JWT): datos, foto de perfil")
class UsuarioProfileController(
    private val userService: UserService,
    private val fileStorageService: FileStorageService,
    private val notificationService: NotificationService
) {

    @Operation(
        summary = "Verificar el rol confirmado de una cuenta por email",
        description = "Un usuario no puede ser propietario y cuidador a la vez: permite a la app comprobar antes de tiempo si esa cuenta ya confirmo un rol."
    )
    @GetMapping("/verificar-rol")
    fun verificarRol(@RequestParam email: String): ResponseEntity<Map<String, Any?>> {
        val user = userService.findByEmail(email.trim().lowercase()).orElse(null)
            ?: return ResponseEntity.ok(mapOf("existe" to false, "rol" to null, "rolConfirmado" to false))
        return ResponseEntity.ok(
            mapOf(
                "existe" to true,
                "rol" to RoleUtil.mapDbRoleToApi(user.rol),
                "rolConfirmado" to (user.rolConfirmado == true)
            )
        )
    }

    @Operation(summary = "Obtener el perfil del usuario autenticado")
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    fun getAuthenticatedProfile(authentication: Authentication): ResponseEntity<UserProfileResponse> {
        val principal = authentication.principal as? com.petcare.security.UserPrincipal
            ?: throw UserNotFoundException("Authenticated user not found")
        val user = userService.findById(principal.id).orElseThrow { UserNotFoundException("User not found") }
        return ResponseEntity.ok(UserProfileResponse.fromUser(user))
    }

    @Operation(summary = "Actualizar el perfil del usuario autenticado", description = "Permite editar nombre, apellido y teléfono.")
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

    @Operation(
        summary = "Registrar/actualizar el token FCM de un usuario",
        description = "Guarda el token de Firebase Cloud Messaging del dispositivo para habilitar notificaciones push. Recibe el id de usuario explicito en el body, igual que el resto de endpoints que consume la app movil (que no envia cabecera Authorization)."
    )
    @PostMapping("/fcm-token")
    fun registerFcmToken(@RequestBody request: Map<String, String?>): ResponseEntity<Any> {
        val usuarioId = (request["usuario_id"] ?: request["usuarioId"])?.toIntOrNull()
            ?: return ResponseEntity.badRequest().body(mapOf("error" to "usuario_id is required"))
        val token = request["token"]?.trim()
        if (token.isNullOrBlank()) {
            return ResponseEntity.badRequest().body(mapOf("error" to "token is required"))
        }
        notificationService.saveFcmToken(usuarioId, token)
        return ResponseEntity.noContent().build()
    }

    @Operation(
        summary = "Activar/desactivar el modo \"no molestar\" de un cuidador",
        description = "Cuando esta activo, el usuario no recibe notificaciones push. Recibe el id de usuario explicito en el body, igual que el resto de endpoints que consume la app movil (que no envia cabecera Authorization)."
    )
    @PutMapping("/no-molestar")
    fun actualizarNoMolestar(@RequestBody request: Map<String, Any?>): ResponseEntity<Any> {
        val usuarioId = (request["usuario_id"] ?: request["usuarioId"])?.toString()?.toIntOrNull()
            ?: return ResponseEntity.badRequest().body(mapOf("error" to "usuario_id is required"))
        val activo = (request["activo"] ?: request["no_molestar"]) as? Boolean
            ?: return ResponseEntity.badRequest().body(mapOf("error" to "activo is required"))
        val user = userService.findById(usuarioId).orElse(null)
            ?: return ResponseEntity.status(404).body(mapOf("error" to "Usuario no encontrado"))
        user.noMolestar = activo
        userService.save(user)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "Subir/reemplazar la foto de perfil del usuario autenticado")
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

    @Operation(summary = "Eliminar la foto de perfil del usuario autenticado")
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

    @Operation(summary = "Servir la imagen de perfil de un usuario por id")
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
