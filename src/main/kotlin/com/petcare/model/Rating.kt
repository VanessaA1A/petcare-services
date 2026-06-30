package com.petcare.model

/*
 * Comentario de modulo PetCare:
 * Entidad de dominio. Representa una tabla o concepto principal usado por la API.
 */

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(
    name = "ratings",
    uniqueConstraints = [UniqueConstraint(columnNames = ["service_request_id", "rated_by_role"])]
)
data class Rating(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(name = "service_request_id", nullable = false)
    var serviceRequestId: Int? = null,

    @Column(name = "caregiver_id", nullable = false)
    var caregiverId: Int? = null,

    @Column(name = "owner_id", nullable = false)
    var ownerId: Int? = null,

    @Column(name = "rated_by_role", nullable = false)
    var ratedByRole: String = "OWNER",

    @Column(nullable = false)
    var score: Double = 5.0,

    @Column(columnDefinition = "text")
    var comment: String? = null,

    @Column(name = "created_at")
    var createdAt: OffsetDateTime? = null
) {
    @PrePersist
    fun prePersist() {
        if (createdAt == null) createdAt = OffsetDateTime.now()
    }
}
