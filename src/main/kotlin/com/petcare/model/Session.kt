package com.petcare.model

import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "sesiones")
data class Session(
    @Id
    var id: UUID? = null,

    @Column(name = "usuario_id", nullable = false)
    var usuarioId: UUID? = null,

    @Column(name = "token_sesion", nullable = false, unique = true)
    var tokenSesion: String? = null,

    @Column(name = "fecha_inicio")
    var fechaInicio: OffsetDateTime? = null,

    @Column(name = "ip_address")
    var ipAddress: String? = null,

    @Column(name = "user_agent")
    var userAgent: String? = null,

    @Column(name = "fecha_fin")
    var fechaFin: OffsetDateTime? = null,

    @Column(name = "logout_explicito")
    var logoutExplicito: Boolean? = false
) {
    @PrePersist
    fun prePersist() {
        if (id == null) id = UUID.randomUUID()
        if (fechaInicio == null) fechaInicio = OffsetDateTime.now()
        if (logoutExplicito == null) logoutExplicito = false
    }
}
