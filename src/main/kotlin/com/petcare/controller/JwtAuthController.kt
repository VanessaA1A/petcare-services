package com.petcare.controller

import com.petcare.dto.LoginRequest
import com.petcare.dto.LoginResponse
import com.petcare.model.User
import com.petcare.security.JwtUtil
import com.petcare.service.AuthService
import com.petcare.service.UserService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api")
class JwtAuthController(
    private val authService: AuthService,
    private val userService: UserService,
    private val jwtUtil: JwtUtil
) {

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<*> {
        val email = request.email.trim()
        val password = request.password

        val userOpt = userService.findByEmail(email)
        if (userOpt.isEmpty) {
            return ResponseEntity.status(404).body(mapOf("error" to "Usuario no encontrado"))
        }

        val user = userOpt.get()
        if (user.passwordHash.isNullOrBlank() || !authService.authenticate(email, password).isPresent) {
            return ResponseEntity.status(401).body(mapOf("error" to "Credenciales inválidas"))
        }

        val token = jwtUtil.generateToken(user.id!!, user.email!!)

        val safeUser = User()
        safeUser.id = user.id
        safeUser.username = user.username
        safeUser.email = user.email
        safeUser.rol = user.rol
        safeUser.createdAt = user.createdAt
        safeUser.lastLogin = user.lastLogin
        safeUser.isActive = user.isActive

        return ResponseEntity.ok(LoginResponse(token, safeUser))
    }
}
