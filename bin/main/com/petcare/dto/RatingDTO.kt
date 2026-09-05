package com.petcare.dto

/*
 * Comentario de modulo PetCare:
 * Objeto de transferencia. Define la forma de los datos que entran o salen por la API.
 */

import com.fasterxml.jackson.annotation.JsonProperty
import com.petcare.model.Rating

data class RatingDTO(
    val id: Int? = null,
    @JsonProperty("service_request_id") val serviceRequestId: Int,
    @JsonProperty("caregiver_id") val caregiverId: Int,
    @JsonProperty("owner_id") val ownerId: Int,
    @JsonProperty("rated_by_role") val ratedByRole: String = "OWNER",
    val score: Double,
    val comment: String? = null,
    @JsonProperty("created_at") val createdAt: String? = null
) {
    fun toEntity(existing: Rating? = null): Rating {
        val rating = existing ?: Rating()
        rating.serviceRequestId = serviceRequestId
        rating.caregiverId = caregiverId
        rating.ownerId = ownerId
        rating.ratedByRole = ratedByRole
        rating.score = score.coerceIn(1.0, 5.0)
        rating.comment = comment
        return rating
    }

    companion object {
        fun fromEntity(entity: Rating) = RatingDTO(
            id = entity.id,
            serviceRequestId = entity.serviceRequestId ?: 0,
            caregiverId = entity.caregiverId ?: 0,
            ownerId = entity.ownerId ?: 0,
            ratedByRole = entity.ratedByRole,
            score = entity.score,
            comment = entity.comment,
            createdAt = entity.createdAt?.toString()
        )
    }
}

data class RatingSummaryDTO(
    val average: Double,
    val count: Int
)
