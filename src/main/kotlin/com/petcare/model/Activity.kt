package com.petcare.model

import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "actividades")
data class Activity(
    @Id
    var id: UUID? = null,

    @Column(name = "sesion_id")
    var sesionId: UUID? = null,

    @Column(name = "usuario_id")
    var usuarioId: UUID? = null,

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
        if (id == null) id = UUID.randomUUID()
        if (fechaHora == null) fechaHora = OffsetDateTime.now()
    }
}
