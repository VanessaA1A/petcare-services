package com.petcare.model

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "favoritos")
data class Favorite(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(name = "usuario_id", nullable = false)
    var usuarioId: Int? = null,

    @Column(name = "cuidador_id")
    var caregiverId: Int? = null,

    @Column(name = "mascota_id")
    var petId: Int? = null,

    @Column(name = "fecha_agregado")
    var addedAt: OffsetDateTime? = null
) {
    @PrePersist
    fun prePersist() {
        if (addedAt == null) addedAt = OffsetDateTime.now()
    }
}
