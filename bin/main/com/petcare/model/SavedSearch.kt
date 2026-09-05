package com.petcare.model

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "busquedas_guardadas")
data class SavedSearch(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(name = "usuario_id", nullable = false)
    var usuarioId: Int? = null,

    @Column(nullable = false)
    var nombre: String? = null,

    @Column(name = "filtros_json", nullable = false, columnDefinition = "jsonb")
    var filtersJson: String? = null,

    @Column(name = "fecha_creacion")
    var createdAt: OffsetDateTime? = null
) {
    @PrePersist
    fun prePersist() {
        if (createdAt == null) createdAt = OffsetDateTime.now()
    }
}
