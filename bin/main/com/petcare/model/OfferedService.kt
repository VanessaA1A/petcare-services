package com.petcare.model

/*
 * Comentario de modulo PetCare:
 * Servicio de negocio. Contiene reglas de PetCare que no deben vivir directamente en los controladores.
 */

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "offered_services")
data class OfferedService(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(name = "caregiver_id", nullable = false)
    var caregiverId: Int? = null,

    @Column(name = "service_type_id", nullable = false)
    var serviceTypeId: Int? = null,

    @Column(nullable = false)
    var title: String? = null,

    var description: String? = null,

    @Column(nullable = false)
    var price: Double? = null,

    @Column(name = "is_available", nullable = false)
    var isAvailable: Boolean = true,

    var latitude: Double? = null,
    var longitude: Double? = null,

    @Column(name = "created_at")
    var createdAt: OffsetDateTime? = null,

    @Column(name = "updated_at")
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
