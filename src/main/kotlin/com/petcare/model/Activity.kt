package com.petcare.model

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "actividades")
data class Activity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(name = "sesion_id")
    var sesionId: Int? = null,

    @Column(name = "usuario_id")
    var usuarioId: Int? = null,

    @Column(name = "tipo_actividad")
    var tipoActividad: String? = null,

    var descripcion: String? = null,

    @Column(name = "ip_address")
    var ipAddress: String? = null,

    @Column(name = "fecha_hora")
    var fechaHora: OffsetDateTime? = null
) {
    @PrePersist
    fun prePersist() {
        if (fechaHora == null) fechaHora = OffsetDateTime.now()
    }
}
