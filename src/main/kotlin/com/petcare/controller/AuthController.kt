package com.petcare.controller

import com.petcare.model.Session
import com.petcare.model.User
import com.petcare.service.ActivityService
import com.petcare.service.AuthService
import com.petcare.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

import java.util.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val userService: UserService,
    private val activityService: ActivityService
) {

    @PostMapping("/login")
    fun login(@RequestBody body: Map<String, String>, @RequestHeader(value = "User-Agent", required = false) userAgent: String?, @RequestHeader(value = "X-Forwarded-For", required = false) xff: String?): ResponseEntity<*> {
        val email = body["email"]
        val password = body["password"]
        if (email == null || password == null) return ResponseEntity.badRequest().body(mapOf("error" to "email and password required"))
        val userOpt = authService.authenticate(email, password)
        if (userOpt.isEmpty) return ResponseEntity.status(401).body(mapOf("error" to "Invalid credentials"))
        val user = userOpt.get()
        val session = authService.createSession(user.id!!, xff ?: "", userAgent)
        user.lastLogin = java.time.OffsetDateTime.now()
        userService.save(user)
        activityService.logActivity(session.id!!, user.id!!, "login", "{\"email\": \"$email\"}", xff ?: userAgent)
        return ResponseEntity.ok(mapOf("user" to user, "session" to session))
    }

    @PostMapping("/recover")
    fun recover(@RequestBody body: Map<String, String>): ResponseEntity<*> {
        val email = body["email"] ?: return ResponseEntity.badRequest().body(mapOf("error" to "email required"))
        val userOpt = userService.findByEmail(email)
        if (userOpt.isEmpty) return ResponseEntity.status(404).body(mapOf("error" to "Email not found"))
        val user = userOpt.get()
        val token = UUID.randomUUID().toString()
        user.resetToken = token
        user.resetTokenExpires = java.time.OffsetDateTime.now().plusHours(1)
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
