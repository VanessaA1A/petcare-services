package com.petcare.controller

/*
 * Comentario de modulo PetCare:
 * Controlador REST. Recibe peticiones HTTP, valida el flujo basico y delega la logica al servicio.
 */

import com.petcare.model.User
import com.petcare.service.ActivityService
import com.petcare.service.AuthService
import com.petcare.service.UserService
import com.petcare.util.RoleUtil
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
@Tag(name = "Usuarios", description = "Administración de usuarios (alta, consulta, roles)")
class UsersController(
    private val userService: UserService,
    private val authService: AuthService,
    private val activityService: ActivityService,
    private val passwordEncoder: org.springframework.security.crypto.password.PasswordEncoder
) {

    @Operation(summary = "Crear un usuario")
    @PostMapping
    fun createUser(@RequestBody body: Map<String, Any>): ResponseEntity<*> {
        val username = (body["username"] as? String)?.trim()
        val email = (body["email"] as? String)?.trim()?.lowercase()
        val password = body["password"] as? String
        val rol = body["rol"] as? String
        if (username.isNullOrBlank() || email.isNullOrBlank() || password.isNullOrBlank()) {
            return ResponseEntity.badRequest().body(mapOf("error" to "username, email and password are required"))
        }
        if (!email.contains("@") || !email.substringAfter('@').contains('.')) {
            return ResponseEntity.badRequest().body(mapOf("error" to "Invalid email format"))
        }
        return try {
            val u = User()
            u.username = username
            u.email = email
            u.passwordHash = passwordEncoder.encode(password)
            u.rol = RoleUtil.normalizeRoleForDatabase(rol) ?: "gestor"
            val saved = userService.create(u)
            return try {
                val session = authService.createSession(saved.id!!, "", null)
                activityService.logActivity(session.id!!, saved.id!!, "register", "username=$username email=$email", null)
                ResponseEntity.status(201).body(mapOf("user" to saved, "session" to session))
            } catch (activityError: Exception) {
                ResponseEntity.status(201).body(mapOf("user" to saved, "warning" to "user created but session/activity failed"))
            }
        } catch (e: Exception) {
            ResponseEntity.status(500).body(mapOf("error" to "Error creating user", "detail" to (e.message ?: "")))
        }
    }

    @Operation(summary = "Listar todos los usuarios")
    @GetMapping
    fun getAll() = ResponseEntity.ok(userService.listAll())

    @Operation(summary = "Obtener un usuario por id")
    @GetMapping("/{id}")
    fun getById(@PathVariable id: Int): ResponseEntity<*> {
        val u = userService.findById(id)
        return if (u.isPresent) ResponseEntity.ok(u.get()) else ResponseEntity.status(404).body(mapOf("error" to "User not found"))
    }

    @Operation(summary = "Actualizar un usuario")
    @PutMapping("/{id}")
    fun updateUser(@PathVariable id: Int, @RequestBody body: Map<String, Any>): ResponseEntity<*> {
        return try {
            val uo = userService.findById(id)
            if (uo.isEmpty) return ResponseEntity.status(404).body(mapOf("error" to "User not found"))
            val u = uo.get()
            if (body.containsKey("username")) u.username = (body["username"] as? String)?.trim()
            if (body.containsKey("email")) u.email = (body["email"] as? String)?.trim()?.lowercase()
            if (body.containsKey("password")) u.passwordHash = passwordEncoder.encode(body["password"] as String)
            if (body.containsKey("rol")) {
                val rejected = applyRoleChange(u, body["rol"] as? String)
                if (rejected != null) return rejected
            }
            if (body.containsKey("is_active")) u.isActive = body["is_active"] as? Boolean
            val saved = userService.save(u)
            ResponseEntity.ok(saved)
        } catch (ex: Exception) {
            ResponseEntity.status(500).body(mapOf("error" to "Error updating user", "detail" to (ex.message ?: "")))
        }
    }

    @Operation(summary = "Eliminar un usuario")
    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: Int): ResponseEntity<*> {
        userService.delete(id)
        return ResponseEntity.noContent().build<Any>()
    }

    @Operation(summary = "Confirmar el rol de un usuario (propietario o cuidador)", description = "Solo se puede usar una vez por cuenta: un usuario no puede ser propietario y cuidador a la vez.")
    @PostMapping("/{id}/roles")
    fun assignRoles(@PathVariable id: Int, @RequestBody body: Map<String, String>): ResponseEntity<*> {
        val role = body["role"] ?: body["rol"] ?: return ResponseEntity.badRequest().body(mapOf("error" to "role is required"))
        val uo = userService.findById(id)
        if (uo.isEmpty) return ResponseEntity.status(404).body(mapOf("error" to "User not found"))
        val u = uo.get()
        val rejected = applyRoleChange(u, role)
        if (rejected != null) return rejected
        val saved = userService.save(u)
        return ResponseEntity.ok(saved)
    }

    /**
     * Regla de negocio: un usuario no puede ser propietario y cuidador a la vez.
     * Se permite elegir el rol una sola vez (rolConfirmado pasa a true); despues de eso,
     * cualquier intento de cambiarlo a un rol distinto se rechaza con 400.
     * Devuelve null si el cambio se aplico sobre `u`, o la respuesta de error a devolver si se rechazo.
     */
    private fun applyRoleChange(u: User, rawRole: String?): ResponseEntity<*>? {
        val normalized = RoleUtil.normalizeRoleForDatabase(rawRole)
            ?: return ResponseEntity.badRequest().body(mapOf("error" to "Rol invalido"))
        if (u.rolConfirmado == true && normalized != u.rol) {
            return ResponseEntity.status(400).body(
                mapOf("error" to "Ya elegiste tu rol (propietario o cuidador) y no puedes cambiarlo. Si necesitas ayuda, contacta a soporte.")
            )
        }
        u.rol = normalized
        u.rolConfirmado = true
        return null
    }
}
