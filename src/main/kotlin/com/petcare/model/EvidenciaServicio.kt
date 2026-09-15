package com.petcare.model

/*
 * Comentario de modulo PetCare:
 * Entidad de dominio. Representa una tabla o concepto principal usado por la API.
 */

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "evidencias_servicio")
data class EvidenciaServicio(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(name = "solicitud_id")
    var solicitudId: Int? = null,

    @Column(nullable = false)
    var tipo: String = "ANTES",

    @Column(name = "imagen_url", nullable = false, columnDefinition = "text")
    var imagenUrl: String = "",

    @Column(columnDefinition = "text")
    var nota: String? = null,

    @Column(columnDefinition = "numeric(10,8)")
    var latitud: Double? = null,

    @Column(columnDefinition = "numeric(11,8)")
    var longitud: Double? = null,

    var fecha: LocalDateTime? = null
) {
    @PrePersist
    fun prePersist() {
        if (fecha == null) fecha = LocalDateTime.now()
    }
}
