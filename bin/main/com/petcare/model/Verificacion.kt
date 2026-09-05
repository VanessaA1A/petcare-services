package com.petcare.model

/*
 * Comentario de modulo PetCare:
 * Entidad de dominio. Representa una tabla o concepto principal usado por la API.
 */

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "verificaciones")
data class Verificacion(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(nullable = false)
    var email: String = "",

    @Column(nullable = false)
    var otp: String = "",

    @Column(name = "fecha_expiracion", nullable = false)
    var fechaExpiracion: OffsetDateTime? = null,

    @Column(nullable = false)
    var usado: Boolean = false,

    @Column(name = "creado_en")
    var creadoEn: OffsetDateTime? = null
) {
    @PrePersist
    fun prePersist() {
        if (creadoEn == null) creadoEn = OffsetDateTime.now()
    }
}
