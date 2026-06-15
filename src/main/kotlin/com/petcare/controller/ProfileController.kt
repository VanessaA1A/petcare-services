package com.petcare.controller

import com.petcare.service.AuthService
import com.petcare.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class ProfileController(private val authService: AuthService, private val userService: UserService) {

    @GetMapping("/profile")
    fun profile(@RequestHeader(value = "Authorization", required = false) auth: String?): ResponseEntity<*> {
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
