package com.petcare.controller

/*
 * Comentario de modulo PetCare:
 * Controlador REST. Recibe peticiones HTTP, valida el flujo basico y delega la logica al servicio.
 */

import com.petcare.model.Session
import com.petcare.model.User
import com.petcare.security.JwtUtil
import com.petcare.service.ActivityService
import com.petcare.service.AuthService
import com.petcare.service.OtpService
import com.petcare.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import java.time.OffsetDateTime
import java.util.*

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Registro, login y sesión de usuarios (autenticación basada en cookies/sesión)")
class AuthController(
    private val authService: AuthService,
    private val userService: UserService,
    private val activityService: ActivityService,
    private val jwtUtil: JwtUtil,
    private val passwordEncoder: PasswordEncoder,
    private val otpService: OtpService
) {

    @Operation(
        summary = "Enviar codigo de verificacion (OTP) al correo",
        description = "El codigo vence en 5 minutos. Sin SMTP configurado, el codigo se registra en el log del servidor y tambien se devuelve en la respuesta (modo de prueba)."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Código generado y enviado (o devuelto en modo de prueba)"),
        ApiResponse(responseCode = "400", description = "Email inválido o ausente")
    ])
    @PostMapping("/send-otp")
    fun sendOtp(@RequestBody body: Map<String, String>): ResponseEntity<*> {
        val email = body["email"]?.trim()?.lowercase()
        if (email.isNullOrBlank() || !email.contains("@")) {
            return ResponseEntity.badRequest().body(mapOf("error" to "Email inválido"))
        }
        val otpModoPrueba = otpService.generarYEnviar(email)
        return ResponseEntity.ok(
            mapOf(
                "message" to "Código enviado" + if (otpModoPrueba != null) " (modo de prueba, sin SMTP configurado)" else "",
                "otp" to otpModoPrueba
            )
        )
    }

    @Operation(summary = "Verificar el codigo OTP enviado a un correo")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Código verificado correctamente"),
        ApiResponse(responseCode = "400", description = "Faltan email/otp, o el código es incorrecto/expirado")
    ])
    @PostMapping("/verify-otp")
    fun verifyOtp(@RequestBody body: Map<String, String>): ResponseEntity<*> {
        val email = body["email"]?.trim()?.lowercase()
        val otp = body["otp"]?.trim()
        if (email.isNullOrBlank() || otp.isNullOrBlank()) {
            return ResponseEntity.badRequest().body(mapOf("error" to "email y otp son requeridos"))
        }
        val verificado = otpService.verificar(email, otp)
        return if (verificado) {
            ResponseEntity.ok(mapOf("verified" to true))
        } else {
            ResponseEntity.status(400).body(mapOf("verified" to false, "error" to "Código incorrecto o expirado"))
        }
    }

    @Operation(summary = "Registrar un nuevo usuario", description = "Crea un usuario con email/contraseña y abre una sesión.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Usuario registrado y sesión creada"),
        ApiResponse(responseCode = "400", description = "Faltan datos, el email es inválido o ya está registrado")
    ])
    @PostMapping("/registro")
    fun register(@RequestBody body: Map<String, String>): ResponseEntity<*> {
        val email = body["email"]?.trim()
        val password = body["password"]
        if (email.isNullOrBlank() || password.isNullOrBlank()) {
            return ResponseEntity.badRequest().body(mapOf("error" to "Email y contraseña son requeridos"))
        }
        if (!email.contains("@")) {
            return ResponseEntity.badRequest().body(mapOf("error" to "Email inválido"))
        }
        if (userService.findByEmail(email).isPresent) {
            return ResponseEntity.badRequest().body(mapOf("error" to "El email ya está registrado"))
        }

        val user = User()
        user.email = email
        user.username = email.substringBefore("@")
        user.passwordHash = passwordEncoder.encode(password)
        // "cliente" no es un valor valido del enum rol_usuario (administrador/propietario/gestor) y
        // hacia fallar el INSERT contra Postgres real (bug encontrado al escribir las pruebas de
        // integracion del Bloque 6.1). Se usa el mismo default que la columna en BD ('gestor') hasta
        // que el usuario confirme su rol via POST /api/users/{id}/roles.
        user.rol = "gestor"

        val saved = userService.create(user)
        val token = jwtUtil.generateToken(saved.id!!, saved.email!!)
        val session = authService.createSession(saved.id!!, token, "", null)
        activityService.logActivity(session.id!!, saved.id!!, "register", "{\"email\": \"$email\"}", null)

        return ResponseEntity.status(201).body(
            mapOf(
                "user" to saved,
                "session" to mapOf("tokenSesion" to session.tokenSesion)
            )
        )
    }

    @Operation(summary = "Iniciar sesión", description = "Valida credenciales y crea una sesión, registrando la actividad de login.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Login exitoso"),
        ApiResponse(responseCode = "400", description = "Email y contraseña son requeridos"),
        ApiResponse(responseCode = "401", description = "Credenciales inválidas"),
        ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    ])
    @PostMapping("/login")
    fun login(@RequestBody body: Map<String, String>, @RequestHeader(value = "User-Agent", required = false) userAgent: String?, @RequestHeader(value = "X-Forwarded-For", required = false) xff: String?): ResponseEntity<*> {
        val email = body["email"]?.trim()
        val password = body["password"]
        if (email.isNullOrBlank() || password.isNullOrBlank()) {
            return ResponseEntity.badRequest().body(mapOf("error" to "Email y contraseña son requeridos"))
        }
        val userOpt = userService.findByEmail(email)
        if (userOpt.isEmpty) {
            return ResponseEntity.status(404).body(mapOf("error" to "Usuario no encontrado"))
        }
        val user = userOpt.get()
        if (!authService.authenticate(email, password).isPresent) {
            return ResponseEntity.status(401).body(mapOf("error" to "Credenciales inválidas"))
        }

        val token = jwtUtil.generateToken(user.id!!, user.email!!)
        user.lastLogin = OffsetDateTime.now()
        userService.save(user)
        val session = authService.createSession(user.id!!, token, xff ?: "", userAgent)
        activityService.logActivity(session.id!!, user.id!!, "login", "{\"email\": \"$email\"}", xff ?: userAgent)

        return ResponseEntity.ok(
            mapOf(
                "user" to user,
                "session" to mapOf("tokenSesion" to session.tokenSesion)
            )
        )
    }

    @Operation(summary = "Solicitar recuperación de contraseña", description = "Genera un token de recuperación temporal para el email indicado.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Token de recuperación generado"),
        ApiResponse(responseCode = "400", description = "El email es requerido"),
        ApiResponse(responseCode = "404", description = "No existe un usuario con ese email")
    ])
    @PostMapping("/recover")
    fun recover(@RequestBody body: Map<String, String>): ResponseEntity<*> {
        val email = body["email"] ?: return ResponseEntity.badRequest().body(mapOf("error" to "email required"))
        val userOpt = userService.findByEmail(email)
        if (userOpt.isEmpty) return ResponseEntity.status(404).body(mapOf("error" to "Email not found"))
        val user = userOpt.get()
        val token = UUID.randomUUID().toString()
        user.resetToken = token
        user.resetTokenExpires = OffsetDateTime.now().plusHours(1)
        userService.save(user)
        return ResponseEntity.ok(mapOf("message" to "Recovery token created", "token" to token))
    }

    @Operation(summary = "Obtener el usuario autenticado", description = "Devuelve el usuario y la sesión asociados al token Bearer enviado.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Usuario y sesión encontrados"),
        ApiResponse(responseCode = "401", description = "Token ausente, inválido o sesión no encontrada")
    ])
    @GetMapping("/me")
    fun me(@RequestHeader(value = "Authorization", required = false) auth: String?): ResponseEntity<*> {
        if (auth == null || !auth.startsWith("Bearer ")) return ResponseEntity.status(401).body(mapOf("error" to "Not authenticated"))
        val token = auth.substring("Bearer ".length)
        val sessionOpt = authService.findSessionByToken(token)
        if (sessionOpt.isEmpty) return ResponseEntity.status(401).body(mapOf("error" to "Not authenticated"))
        val session = sessionOpt.get()
        val userOpt = userService.findById(session.usuarioId!!)
        if (userOpt.isEmpty) return ResponseEntity.status(401).body(mapOf("error" to "Not authenticated"))
        return ResponseEntity.ok(mapOf("user" to userOpt.get(), "session" to session))
    }
}
