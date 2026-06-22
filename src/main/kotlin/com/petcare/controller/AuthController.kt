package com.petcare.controller

import com.petcare.model.Session
import com.petcare.model.User
import com.petcare.security.JwtUtil
import com.petcare.service.ActivityService
import com.petcare.service.AuthService
import com.petcare.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import java.time.OffsetDateTime
import java.util.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val userService: UserService,
    private val activityService: ActivityService,
    private val jwtUtil: JwtUtil,
    private val passwordEncoder: PasswordEncoder
) {

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
