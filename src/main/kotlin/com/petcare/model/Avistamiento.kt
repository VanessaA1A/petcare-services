package com.petcare.model

/*
 * Comentario de modulo PetCare:
 * Entidad de dominio. Representa una tabla o concepto principal usado por la API.
 */

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "avistamientos")
data class Avistamiento(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(name = "alerta_id")
    var alertaId: Int? = null,

    @Column(name = "usuario_id")
    var usuarioId: Int? = null,

    @Column(columnDefinition = "numeric(10,8)")
    var latitud: Double? = null,

    @Column(columnDefinition = "numeric(11,8)")
    var longitud: Double? = null,

    @Column(columnDefinition = "text")
    var comentario: String? = null,

    @Column(name = "imagen_url", columnDefinition = "text")
    var imagenUrl: String? = null,

    var fecha: LocalDateTime? = null
) {
    @PrePersist
    fun prePersist() {
        if (fecha == null) fecha = LocalDateTime.now()
    }
}
