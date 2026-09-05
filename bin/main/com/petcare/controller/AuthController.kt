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
        user.rol = "cliente"

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
