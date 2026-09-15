package com.petcare.model

/*
 * Comentario de modulo PetCare:
 * Entidad de dominio. Representa una tabla o concepto principal usado por la API.
 */

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "alertas_perdida")
data class AlertaPerdida(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(name = "pets_id")
    var petsId: Int? = null,

    @Column(name = "usuario_id")
    var usuarioId: Int? = null,

    @Column(columnDefinition = "text")
    var descripcion: String? = null,

    @Column(columnDefinition = "numeric(10,8)")
    var latitud: Double? = null,

    @Column(columnDefinition = "numeric(11,8)")
    var longitud: Double? = null,

    @Column(name = "direccion_texto")
    var direccionTexto: String? = null,

    var estado: String = "ACTIVA",

    @Column(name = "fecha_creacion")
    var fechaCreacion: LocalDateTime? = null,

    @Column(name = "fecha_cierre")
    var fechaCierre: LocalDateTime? = null
) {
    @PrePersist
    fun prePersist() {
        if (fechaCreacion == null) fechaCreacion = LocalDateTime.now()
    }
}
