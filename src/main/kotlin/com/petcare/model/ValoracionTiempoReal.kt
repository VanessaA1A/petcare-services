package com.petcare.model

/*
 * Comentario de modulo PetCare:
 * Entidad de dominio. Representa una tabla o concepto principal usado por la API.
 */

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "valoraciones_tiempo_real")
data class ValoracionTiempoReal(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(name = "service_request_id", nullable = false)
    var serviceRequestId: Int? = null,

    @Column(name = "usuario_id", nullable = false)
    var usuarioId: Int? = null,

    @Column(name = "tipo_reaccion", nullable = false)
    var tipoReaccion: String = "CORAZON",

    @Column(name = "created_at")
    var createdAt: OffsetDateTime? = null
) {
    @PrePersist
    fun prePersist() {
        if (createdAt == null) createdAt = OffsetDateTime.now()
    }
}
