package com.petcare.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.petcare.model.OfferedService
import com.petcare.model.ServiceApplication
import com.petcare.model.ServiceRequest

data class OfferedServiceDTO(
    val id: Int? = null,
    @JsonProperty("caregiver_id") val caregiverId: Int = 0,
    @JsonProperty("service_type_id") val serviceTypeId: Int = 0,
    val title: String = "",
    val description: String? = null,
    val price: Double = 0.0,
    @JsonProperty("is_available") val isAvailable: Boolean = true,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @JsonProperty("created_at") val createdAt: String? = null
) {
    fun toEntity(existing: OfferedService? = null): OfferedService {
        val service = existing ?: OfferedService()
        service.caregiverId = caregiverId
        service.serviceTypeId = serviceTypeId
        service.title = title
        service.description = description
        service.price = price
        service.isAvailable = isAvailable
        service.latitude = latitude
        service.longitude = longitude
        return service
    }

    companion object {
        fun fromEntity(entity: OfferedService) = OfferedServiceDTO(
            id = entity.id,
            caregiverId = entity.caregiverId ?: 0,
            serviceTypeId = entity.serviceTypeId ?: 0,
            title = entity.title.orEmpty(),
            description = entity.description,
            price = entity.price ?: 0.0,
            isAvailable = entity.isAvailable,
            latitude = entity.latitude,
            longitude = entity.longitude,
            createdAt = entity.createdAt?.toString()
        )
    }
}

data class ServiceRequestDTO(
    val id: Int = 0,
    @JsonProperty("owner_id") val ownerId: Int = 0,
    @JsonProperty("pet_id") val petId: Int = 0,
    @JsonProperty("pet_ids") val petIds: List<Int>? = emptyList(),
    @JsonProperty("service_type_id") val serviceTypeId: Int = 0,
    val title: String = "",
    val description: String? = null,
    @JsonProperty("requested_date") val requestedDate: String? = null,
    @JsonProperty("start_time") val startTime: String? = null,
    @JsonProperty("end_time") val endTime: String? = null,
    val status: String = "PENDING",
    @JsonProperty("offered_service_id") val offeredServiceId: Int? = null,
    @JsonProperty("source_type") val sourceType: String = "OPEN",
    val latitude: Double? = null,
    val longitude: Double? = null,
    @JsonProperty("created_at") val createdAt: String? = null
) {
    fun toEntity(existing: ServiceRequest? = null): ServiceRequest {
        val request = existing ?: ServiceRequest()
        request.id = id.takeIf { it > 0 }
        request.ownerId = ownerId
        request.petId = petId
        request.petIds = (petIds?.takeIf { it.isNotEmpty() } ?: listOf(petId)).joinToString(",")
        request.serviceTypeId = serviceTypeId
        request.title = title
        request.description = description
        request.requestedDate = requestedDate
        request.startTime = startTime
        request.endTime = endTime
        request.status = status
        request.offeredServiceId = offeredServiceId
        request.sourceType = sourceType
        request.latitude = latitude
        request.longitude = longitude
        return request
    }

    companion object {
        fun fromEntity(entity: ServiceRequest): ServiceRequestDTO {
            val ids = entity.petIds
                ?.split(',')
                ?.mapNotNull { it.trim().toIntOrNull() }
                ?.ifEmpty { null }
                ?: listOfNotNull(entity.petId)
            return ServiceRequestDTO(
                id = entity.id ?: 0,
                ownerId = entity.ownerId ?: 0,
                petId = entity.petId ?: 0,
                petIds = ids,
                serviceTypeId = entity.serviceTypeId ?: 0,
                title = entity.title.orEmpty(),
                description = entity.description,
                requestedDate = entity.requestedDate,
                startTime = entity.startTime,
                endTime = entity.endTime,
                status = entity.status,
                offeredServiceId = entity.offeredServiceId,
                sourceType = entity.sourceType,
                latitude = entity.latitude,
                longitude = entity.longitude,
                createdAt = entity.createdAt?.toString()
            )
        }
    }
}

data class ServiceApplicationDTO(
    val id: Int? = null,
    @JsonProperty("service_request_id") val serviceRequestId: Int = 0,
    @JsonProperty("caregiver_id") val caregiverId: Int = 0,
    @JsonProperty("offered_service_id") val offeredServiceId: Int? = null,
    @JsonProperty("initiated_by") val initiatedBy: String = "CAREGIVER",
    val status: String = "PENDING",
    @JsonProperty("owner_name") val ownerName: String? = null,
    @JsonProperty("caregiver_name") val caregiverName: String? = null,
    @JsonProperty("created_at") val createdAt: String? = null
) {
    fun toEntity(existing: ServiceApplication? = null): ServiceApplication {
        val application = existing ?: ServiceApplication()
        application.serviceRequestId = serviceRequestId
        application.caregiverId = caregiverId
        application.offeredServiceId = offeredServiceId
        application.initiatedBy = initiatedBy
        application.status = status
        return application
    }

    companion object {
        fun fromEntity(
            entity: ServiceApplication,
            ownerName: String? = null,
            caregiverName: String? = null
        ) = ServiceApplicationDTO(
            id = entity.id,
            serviceRequestId = entity.serviceRequestId ?: 0,
            caregiverId = entity.caregiverId ?: 0,
            offeredServiceId = entity.offeredServiceId,
            initiatedBy = entity.initiatedBy,
            status = entity.status,
            ownerName = ownerName,
            caregiverName = caregiverName,
            createdAt = entity.createdAt?.toString()
        )
    }
}

data class StatusUpdateRequest(val status: String = "")
data class ScheduleUpdateRequest(@JsonProperty("requested_date") val requestedDate: String? = null, @JsonProperty("start_time") val startTime: String? = null, @JsonProperty("end_time") val endTime: String? = null)
