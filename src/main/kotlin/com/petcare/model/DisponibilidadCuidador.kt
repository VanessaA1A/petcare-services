package com.petcare.model

/*
 * Comentario de modulo PetCare:
 * Entidad de dominio. Representa una tabla o concepto principal usado por la API.
 */

import jakarta.persistence.*
import java.time.LocalDateTime
import java.time.LocalTime

@Entity
@Table(name = "disponibilidad_cuidador")
data class DisponibilidadCuidador(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(name = "cuidador_id")
    var cuidadorId: Int? = null,

    @Column(name = "dia_semana", nullable = false)
    var diaSemana: Int = 0,

    @Column(name = "hora_inicio", nullable = false)
    var horaInicio: LocalTime = LocalTime.MIDNIGHT,

    @Column(name = "hora_fin", nullable = false)
    var horaFin: LocalTime = LocalTime.MIDNIGHT,

    @Column(nullable = false)
    var activo: Boolean = true,

    @Column(name = "fecha_creacion")
    var fechaCreacion: LocalDateTime? = null
) {
    @PrePersist
    fun prePersist() {
        if (fechaCreacion == null) fechaCreacion = LocalDateTime.now()
    }
}
