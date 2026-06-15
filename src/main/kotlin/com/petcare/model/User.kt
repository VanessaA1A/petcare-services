package com.petcare.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.petcare.util.RoleUtil
import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "usuarios")
class User {
    @Id
    var id: UUID? = null

    @Column(unique = true, nullable = false)
    var username: String? = null

    @Column(unique = true, nullable = false)
    var email: String? = null

    @get:JsonIgnore
    @Column(name = "password_hash", nullable = false)
    var passwordHash: String? = null

    @get:JsonIgnore
    @Column(name = "rol")
    var rol: String? = null

    @get:JsonProperty("rol")
    val normalizedRol: String?
        get() = RoleUtil.mapDbRoleToApi(rol)

    @get:JsonProperty("role")
    val role: String?
        get() = RoleUtil.mapDbRoleToApi(rol)

    @Column(name = "created_at")
    var createdAt: OffsetDateTime? = null

    @Column(name = "last_login")
    var lastLogin: OffsetDateTime? = null

    @Column(name = "is_active")
    var isActive: Boolean? = true

    @get:JsonIgnore
    @Column(name = "reset_token")
    var resetToken: String? = null

    @get:JsonIgnore
    @Column(name = "reset_token_expires")
    var resetTokenExpires: OffsetDateTime? = null

    @PrePersist
    fun prePersist() {
        if (id == null) id = UUID.randomUUID()
        if (createdAt == null) createdAt = OffsetDateTime.now()
        if (isActive == null) isActive = true
    }
}
