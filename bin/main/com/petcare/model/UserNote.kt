package com.petcare.model

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "notas_usuario")
data class UserNote(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(name = "propietario_id", nullable = false)
    var ownerId: Int? = null,

    @Column(name = "objetivo_id", nullable = false)
    var targetId: Int? = null,

    @Column(nullable = false, columnDefinition = "text")
    var nota: String? = null,

    @Column(name = "fecha_creacion")
    var createdAt: OffsetDateTime? = null,

    @Column(name = "fecha_actualizacion")
    var updatedAt: OffsetDateTime? = null
) {
    @PrePersist
    fun prePersist() {
        val now = OffsetDateTime.now()
        if (createdAt == null) createdAt = now
        if (updatedAt == null) updatedAt = now
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = OffsetDateTime.now()
    }
}
