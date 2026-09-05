package com.petcare.model

/*
 * Comentario de modulo PetCare:
 * Servicio de negocio. Contiene reglas de PetCare que no deben vivir directamente en los controladores.
 */

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(
    name = "service_applications",
    uniqueConstraints = [UniqueConstraint(columnNames = ["service_request_id", "caregiver_id"])]
)
data class ServiceApplication(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(name = "service_request_id", nullable = false)
    var serviceRequestId: Int? = null,

    @Column(name = "caregiver_id", nullable = false)
    var caregiverId: Int? = null,

    @Column(name = "offered_service_id")
    var offeredServiceId: Int? = null,

    @Column(name = "initiated_by", nullable = false)
    var initiatedBy: String = "CAREGIVER",

    @Column(nullable = false)
    var status: String = "PENDING",

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
