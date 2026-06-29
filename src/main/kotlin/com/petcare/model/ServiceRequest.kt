package com.petcare.model

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "service_requests")
data class ServiceRequest(
    @Id
    var id: Int? = null,

    @Column(name = "owner_id", nullable = false)
    var ownerId: Int? = null,

    @Column(name = "pet_id", nullable = false)
    var petId: Int? = null,

    @Column(name = "pet_ids")
    var petIds: String? = null,

    @Column(name = "service_type_id", nullable = false)
    var serviceTypeId: Int? = null,

    @Column(nullable = false)
    var title: String? = null,

    @Column(columnDefinition = "text")
    var description: String? = null,

    @Column(name = "requested_date")
    var requestedDate: String? = null,

    @Column(name = "start_time")
    var startTime: String? = null,

    @Column(name = "end_time")
    var endTime: String? = null,

    @Column(nullable = false)
    var status: String = "PENDING",

    @Column(name = "offered_service_id")
    var offeredServiceId: Int? = null,

    @Column(name = "source_type", nullable = false)
    var sourceType: String = "OPEN",

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
