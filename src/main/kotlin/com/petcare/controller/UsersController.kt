package com.petcare.controller

import com.petcare.model.User
import com.petcare.service.ActivityService
import com.petcare.service.AuthService
import com.petcare.service.UserService
import com.petcare.util.HashUtil
import com.petcare.util.RoleUtil
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

import java.util.*

@RestController
@RequestMapping("/api/users")
class UsersController(
    private val userService: UserService,
    private val authService: AuthService,
    private val activityService: ActivityService
) {

    @PostMapping
    fun createUser(@RequestBody body: Map<String, Any>): ResponseEntity<*> {
        val username = body["username"] as? String
        val email = body["email"] as? String
        val password = body["password"] as? String
        val rol = body["rol"] as? String
        if (username == null || email == null || password == null) return ResponseEntity.badRequest().body(mapOf("error" to "username, email and password are required"))
        return try {
            val u = User()
            u.username = username
            u.email = email
            u.passwordHash = HashUtil.md5(password)
            u.rol = RoleUtil.normalizeRoleForDatabase(rol) ?: "gestor"
            val saved = userService.create(u)
            return try {
                val session = authService.createSession(saved.id!!, "", null)
                activityService.logActivity(session.id!!, saved.id!!, "register", "{\"username\": \"$username\", \"email\": \"$email\"}", null)
                ResponseEntity.status(201).body(mapOf("user" to saved, "session" to session))
            } catch (activityError: Exception) {
                ResponseEntity.status(201).body(mapOf("user" to saved, "warning" to "user created but session/activity failed"))
            }
        } catch (e: Exception) {
            ResponseEntity.status(500).body(mapOf("error" to "Error creating user", "detail" to (e.message ?: "")))
        }
    }

    @GetMapping
    fun getAll() = ResponseEntity.ok(userService.listAll())

    @GetMapping("/{id}")
    fun getById(@PathVariable id: String): ResponseEntity<*> {
        return try {
            val uuid = UUID.fromString(id)
            val u = userService.findById(uuid)
            if (u.isPresent) ResponseEntity.ok(u.get()) else ResponseEntity.status(404).body(mapOf("error" to "User not found"))
        } catch (ex: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to "Invalid id"))
        }
    }

    @PutMapping("/{id}")
    fun updateUser(@PathVariable id: String, @RequestBody body: Map<String, Any>): ResponseEntity<*> {
        return try {
            val uuid = UUID.fromString(id)
            val uo = userService.findById(uuid)
            if (uo.isEmpty) return ResponseEntity.status(404).body(mapOf("error" to "User not found"))
            val u = uo.get()
            if (body.containsKey("username")) u.username = body["username"] as? String
            if (body.containsKey("email")) u.email = body["email"] as? String
            if (body.containsKey("password")) u.passwordHash = HashUtil.md5(body["password"] as String)
            if (body.containsKey("rol")) u.rol = RoleUtil.normalizeRoleForDatabase(body["rol"] as? String) ?: u.rol
            if (body.containsKey("is_active")) u.isActive = body["is_active"] as? Boolean
            val saved = userService.save(u)
            ResponseEntity.ok(saved)
        } catch (ex: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to "Invalid id"))
        } catch (ex: Exception) {
            ResponseEntity.status(500).body(mapOf("error" to "Error updating user", "detail" to (ex.message ?: "")))
        }
    }

    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: String): ResponseEntity<*> {
        return try {
            val uuid = UUID.fromString(id)
            userService.delete(uuid)
            ResponseEntity.noContent().build<Any>()
        } catch (ex: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to "Invalid id"))
        }
    }

    @PostMapping("/{id}/roles")
    fun assignRoles(@PathVariable id: String, @RequestBody body: Map<String, String>): ResponseEntity<*> {
        val role = body["role"] ?: return ResponseEntity.badRequest().body(mapOf("error" to "role is required"))
        return try {
            val uuid = UUID.fromString(id)
            val uo = userService.findById(uuid)
            if (uo.isEmpty) return ResponseEntity.status(404).body(mapOf("error" to "User not found"))
            val u = uo.get()
            u.rol = RoleUtil.normalizeRoleForDatabase(role) ?: u.rol
            val saved = userService.save(u)
            ResponseEntity.ok(saved)
        } catch (ex: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to "Invalid id"))
        }
    }
}
