package com.petcare.controller

/*
 * Comentario de modulo PetCare:
 * Controlador REST. Recibe peticiones HTTP, valida el flujo basico y delega la logica al servicio.
 */

import com.petcare.security.AdminGuard
import com.petcare.service.AuthService
import com.petcare.security.JwtUtil
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Bloque 7/13: login propio para el panel de administracion. Reutiliza AuthService/JwtUtil
 * (la misma infraestructura de login del resto de la app) pero exige ademas que el usuario
 * tenga rol "administrador" - un propietario o cuidador con credenciales validas NO puede
 * entrar por aqui, aunque su email/password sean correctos.
 */
@RestController
@RequestMapping("/api/admin/auth")
@Tag(name = "Admin Auth", description = "Login del panel de administración (Bloque 7/13)")
class AdminAuthController(
    private val authService: AuthService,
    private val jwtUtil: JwtUtil,
    private val adminGuard: AdminGuard
) {
    @Operation(summary = "Iniciar sesión como administrador", description = "Requiere que la cuenta tenga rol administrador.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Login exitoso"),
        ApiResponse(responseCode = "400", description = "Email y contraseña son requeridos"),
        ApiResponse(responseCode = "401", description = "Credenciales inválidas"),
        ApiResponse(responseCode = "403", description = "La cuenta no tiene rol administrador")
    ])
    @PostMapping("/login")
    fun login(@RequestBody body: Map<String, String>): ResponseEntity<*> {
        val email = body["email"]?.trim()
        val password = body["password"]
        if (email.isNullOrBlank() || password.isNullOrBlank()) {
            return ResponseEntity.badRequest().body(mapOf("error" to "Email y contraseña son requeridos"))
        }
        val user = authService.authenticate(email, password).orElse(null)
            ?: return ResponseEntity.status(401).body(mapOf("error" to "Credenciales inválidas"))
        if (user.rol != "administrador") {
            return ResponseEntity.status(403).body(mapOf("error" to "Esta cuenta no tiene permisos de administrador"))
        }

        val token = jwtUtil.generateToken(user.id!!, user.email!!)
        val session = authService.createSession(user.id!!, token, "", null)
        return ResponseEntity.ok(
            mapOf(
                "token" to token,
                "user" to mapOf("id" to user.id, "email" to user.email, "nombre" to user.nombre),
                "sessionId" to session.id
            )
        )
    }

    @Operation(summary = "Cerrar sesión de administrador")
    @ApiResponses(value = [ApiResponse(responseCode = "204", description = "Sesión cerrada")])
    @PostMapping("/logout")
    fun logout(@RequestHeader(value = "Authorization", required = false) auth: String?): ResponseEntity<*> {
        val token = auth?.takeIf { it.startsWith("Bearer ") }?.substring("Bearer ".length)
        if (token != null) authService.logout(token)
        return ResponseEntity.noContent().build<Any>()
    }

    @Operation(summary = "Obtener el administrador autenticado")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Administrador autenticado"),
        ApiResponse(responseCode = "401", description = "Token ausente, inválido o sesión no encontrada"),
        ApiResponse(responseCode = "403", description = "La cuenta no tiene rol administrador")
    ])
    @GetMapping("/me")
    fun me(@RequestHeader(value = "Authorization", required = false) auth: String?): ResponseEntity<*> {
        val user = adminGuard.adminFromBearer(auth)
            ?: return ResponseEntity.status(401).body(mapOf("error" to "No autenticado como administrador"))
        return ResponseEntity.ok(mapOf("id" to user.id, "email" to user.email, "nombre" to user.nombre))
    }
}
