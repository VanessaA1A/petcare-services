package com.petcare.model

/*
 * Comentario de modulo PetCare:
 * Entidad de dominio. Representa una tabla o concepto principal usado por la API.
 */

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "sesiones")
data class Session(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(name = "usuario_id", nullable = false)
    var usuarioId: Int? = null,

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
        if (fechaInicio == null) fechaInicio = OffsetDateTime.now()
        if (logoutExplicito == null) logoutExplicito = false
    }
}
