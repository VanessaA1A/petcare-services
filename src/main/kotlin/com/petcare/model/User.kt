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
    @JvmField
    var id: UUID? = null

    @Column(unique = true, nullable = false)
    @JvmField
    var username: String? = null

    @Column(unique = true, nullable = false)
    @JvmField
    var email: String? = null

    @get:JsonIgnore
    @Column(name = "password_hash", nullable = false)
    @JvmField
    var passwordHash: String? = null

    @get:JsonIgnore
    @Column(name = "rol")
    @JvmField
    var rol: String? = null

    @Column(name = "nombre")
    @JvmField
    var nombre: String? = null

    @Column(name = "apellido")
    @JvmField
    var apellido: String? = null

    @Column(name = "telefono")
    @JvmField
    var telefono: String? = null

    @get:JsonIgnore
    @Column(name = "foto_perfil_filename")
    @JvmField
    var fotoPerfilFilename: String? = null

    @Column(name = "foto_perfil_url")
    @JvmField
    var fotoPerfilUrl: String? = null

    @get:JsonProperty("rol")
    val normalizedRol: String?
        get() = RoleUtil.mapDbRoleToApi(rol)

    @get:JsonProperty("role")
    val role: String?
        get() = RoleUtil.mapDbRoleToApi(rol)

    @Column(name = "created_at")
    @JvmField
    var createdAt: OffsetDateTime? = null

    @Column(name = "last_login")
    @JvmField
    var lastLogin: OffsetDateTime? = null

    @Column(name = "is_active")
    @JvmField
    var isActive: Boolean? = true

    @get:JsonIgnore
    @Column(name = "reset_token")
    @JvmField
    var resetToken: String? = null

    @get:JsonIgnore
    @Column(name = "reset_token_expires")
    @JvmField
    var resetTokenExpires: OffsetDateTime? = null

    @PrePersist
    fun prePersist() {
        if (id == null) id = UUID.randomUUID()
        if (createdAt == null) createdAt = OffsetDateTime.now()
        if (isActive == null) isActive = true
    }
}
