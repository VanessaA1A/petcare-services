package com.petcare.dto

/*
 * Comentario de modulo PetCare:
 * Objeto de transferencia. Define la forma de los datos que entran o salen por la API.
 */

import com.fasterxml.jackson.annotation.JsonProperty
import com.petcare.model.Rating
import io.swagger.v3.oas.annotations.media.Schema

data class RatingDTO(
    @Schema(example = "42") val id: Int? = null,
    @JsonProperty("service_request_id") @Schema(example = "1001") val serviceRequestId: Int,
    @JsonProperty("caregiver_id") @Schema(example = "22") val caregiverId: Int,
    @JsonProperty("owner_id") @Schema(example = "17") val ownerId: Int,
    @JsonProperty("rated_by_role") @Schema(example = "OWNER") val ratedByRole: String = "OWNER",
    @Schema(example = "4.5") val score: Double,
    @Schema(example = "Excelente atencion, muy puntual.") val comment: String? = null,
    @JsonProperty("created_at") @Schema(example = "2026-09-05T18:45:00Z") val createdAt: String? = null
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
    @Schema(example = "4.7") val average: Double,
    @Schema(example = "15") val count: Int
)
