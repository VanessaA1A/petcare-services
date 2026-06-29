package com.petcare.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.petcare.util.RoleUtil
import jakarta.persistence.*
import org.hibernate.annotations.ColumnTransformer
import java.time.OffsetDateTime

@Entity
@Table(name = "usuarios")
class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JvmField
    var id: Int? = null

    @Column(unique = true, nullable = false)
    @JvmField
    var username: String? = null

    @Column(unique = true, nullable = false)
    @JvmField
    var email: String? = null

    @get:JsonIgnore
    @field:JsonIgnore
    @Column(name = "password_hash", nullable = false)
    @JvmField
    var passwordHash: String? = null

    @get:JsonIgnore
    @field:JsonIgnore
    @Column(name = "rol", columnDefinition = "rol_usuario")
    @ColumnTransformer(write = "?::rol_usuario")
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
    @field:JsonIgnore
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
    @field:JsonIgnore
    @Column(name = "reset_token")
    @JvmField
    var resetToken: String? = null

    @get:JsonIgnore
    @field:JsonIgnore
    @Column(name = "reset_token_expires")
    @JvmField
    var resetTokenExpires: OffsetDateTime? = null

    @PrePersist
    fun prePersist() {
        if (createdAt == null) createdAt = OffsetDateTime.now()
        if (isActive == null) isActive = true
    }
}
