package com.petcare.dto

/*
 * Comentario de modulo PetCare:
 * Servicio de negocio. Contiene reglas de PetCare que no deben vivir directamente en los controladores.
 */

import com.fasterxml.jackson.annotation.JsonProperty
import com.petcare.model.OfferedService
import com.petcare.model.ServiceApplication
import com.petcare.model.ServiceRequest
import io.swagger.v3.oas.annotations.media.Schema

data class OfferedServiceDTO(
    @Schema(example = "12") val id: Int? = null,
    @JsonProperty("caregiver_id") @Schema(example = "22") val caregiverId: Int = 0,
    @JsonProperty("service_type_id") @Schema(example = "3") val serviceTypeId: Int = 0,
    @Schema(example = "Paseo de perros en el barrio") val title: String = "",
    @Schema(example = "Paseos de 30 a 60 minutos, incluye agua y bolsas para desechos.") val description: String? = null,
    @Schema(example = "150.0") val price: Double = 0.0,
    @JsonProperty("is_available") @Schema(example = "true") val isAvailable: Boolean = true,
    @Schema(example = "12.1364") val latitude: Double? = null,
    @Schema(example = "-86.2514") val longitude: Double? = null,
    @JsonProperty("created_at") @Schema(example = "2026-06-01T08:00:00Z") val createdAt: String? = null,
    @JsonProperty("caregiver_badge") @Schema(example = "CONFIABLE") val caregiverBadge: String? = null
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
        fun fromEntity(entity: OfferedService, caregiverBadge: String? = null) = OfferedServiceDTO(
            id = entity.id,
            caregiverId = entity.caregiverId ?: 0,
            serviceTypeId = entity.serviceTypeId ?: 0,
            title = entity.title.orEmpty(),
            description = entity.description,
            price = entity.price ?: 0.0,
            isAvailable = entity.isAvailable,
            latitude = entity.latitude,
            longitude = entity.longitude,
            createdAt = entity.createdAt?.toString(),
            caregiverBadge = caregiverBadge
        )
    }
}

data class ServiceRequestDTO(
    @Schema(example = "1001") val id: Int = 0,
    @JsonProperty("owner_id") @Schema(example = "17") val ownerId: Int = 0,
    @JsonProperty("pet_id") @Schema(example = "4") val petId: Int = 0,
    @JsonProperty("pet_ids") @Schema(example = "[4]") val petIds: List<Int>? = emptyList(),
    @JsonProperty("service_type_id") @Schema(example = "3") val serviceTypeId: Int = 0,
    @Schema(example = "Necesito paseador para el sabado") val title: String = "",
    @Schema(example = "Mi perro necesita un paseo de una hora en la tarde.") val description: String? = null,
    @JsonProperty("requested_date") @Schema(example = "2026-09-20") val requestedDate: String? = null,
    @JsonProperty("start_time") @Schema(example = "15:00") val startTime: String? = null,
    @JsonProperty("end_time") @Schema(example = "16:00") val endTime: String? = null,
    @Schema(example = "PENDING") val status: String = "PENDING",
    @JsonProperty("offered_service_id") @Schema(example = "12") val offeredServiceId: Int? = null,
    @JsonProperty("source_type") @Schema(example = "OPEN") val sourceType: String = "OPEN",
    @Schema(example = "12.1364") val latitude: Double? = null,
    @Schema(example = "-86.2514") val longitude: Double? = null,
    @JsonProperty("created_at") @Schema(example = "2026-09-14T12:00:00Z") val createdAt: String? = null,
    @JsonProperty("owner_badge") @Schema(example = "CONFIABLE") val ownerBadge: String? = null
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
        fun fromEntity(entity: ServiceRequest, ownerBadge: String? = null): ServiceRequestDTO {
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
                createdAt = entity.createdAt?.toString(),
                ownerBadge = ownerBadge
            )
        }
    }
}

data class ServiceApplicationDTO(
    @Schema(example = "301") val id: Int? = null,
    @JsonProperty("service_request_id") @Schema(example = "1001") val serviceRequestId: Int = 0,
    @JsonProperty("caregiver_id") @Schema(example = "22") val caregiverId: Int = 0,
    @JsonProperty("offered_service_id") @Schema(example = "12") val offeredServiceId: Int? = null,
    @JsonProperty("initiated_by") @Schema(example = "CAREGIVER") val initiatedBy: String = "CAREGIVER",
    @Schema(example = "PENDING") val status: String = "PENDING",
    @JsonProperty("owner_name") @Schema(example = "Maria Propietaria") val ownerName: String? = null,
    @JsonProperty("caregiver_name") @Schema(example = "Carlos Cuidador") val caregiverName: String? = null,
    @JsonProperty("owner_phone") @Schema(example = "+505 8888 1111") val ownerPhone: String? = null,
    @JsonProperty("owner_email") @Schema(example = "maria@petcare.local") val ownerEmail: String? = null,
    @JsonProperty("caregiver_phone") @Schema(example = "+505 8888 2222") val caregiverPhone: String? = null,
    @JsonProperty("caregiver_email") @Schema(example = "carlos@petcare.local") val caregiverEmail: String? = null,
    @JsonProperty("created_at") @Schema(example = "2026-09-14T09:30:00Z") val createdAt: String? = null,
    @JsonProperty("owner_badge") @Schema(example = "CONFIABLE") val ownerBadge: String? = null,
    @JsonProperty("caregiver_badge") @Schema(example = "EXPERIMENTADO") val caregiverBadge: String? = null
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
            caregiverName: String? = null,
            ownerPhone: String? = null,
            ownerEmail: String? = null,
            caregiverPhone: String? = null,
            caregiverEmail: String? = null,
            ownerBadge: String? = null,
            caregiverBadge: String? = null
        ) = ServiceApplicationDTO(
            id = entity.id,
            serviceRequestId = entity.serviceRequestId ?: 0,
            caregiverId = entity.caregiverId ?: 0,
            offeredServiceId = entity.offeredServiceId,
            initiatedBy = entity.initiatedBy,
            status = entity.status,
            ownerName = ownerName,
            caregiverName = caregiverName,
            ownerPhone = ownerPhone,
            ownerEmail = ownerEmail,
            caregiverPhone = caregiverPhone,
            caregiverEmail = caregiverEmail,
            createdAt = entity.createdAt?.toString(),
            ownerBadge = ownerBadge,
            caregiverBadge = caregiverBadge
        )
    }
}

data class StatusUpdateRequest(@Schema(example = "ACCEPTED") val status: String = "")
data class ScheduleUpdateRequest(
    @JsonProperty("requested_date") @Schema(example = "2026-09-20") val requestedDate: String? = null,
    @JsonProperty("start_time") @Schema(example = "15:00") val startTime: String? = null,
    @JsonProperty("end_time") @Schema(example = "16:00") val endTime: String? = null
)
