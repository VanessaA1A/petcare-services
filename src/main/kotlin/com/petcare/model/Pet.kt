package com.petcare.model

import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "pets")
data class Pet(
    @Id
    var id: UUID? = null,

    @Column(name = "owner_id", nullable = false)
    var ownerId: UUID? = null,

    @Column(nullable = false)
    var name: String? = null,

    var species: String? = null,
    var breed: String? = null,
    var size: String? = null,
    var age: Int? = null,
    var weight: java.math.BigDecimal? = null,
    var description: String? = null,

    @Column(name = "created_at")
    var createdAt: OffsetDateTime? = null,

    @Column(name = "updated_at")
    var updatedAt: OffsetDateTime? = null
) {
    @PrePersist
    fun prePersist() {
        if (id == null) id = UUID.randomUUID()
        if (createdAt == null) createdAt = OffsetDateTime.now()
        if (updatedAt == null) updatedAt = OffsetDateTime.now()
    }

    @PreUpdate
    fun preUpdate() { updatedAt = OffsetDateTime.now() }
}
