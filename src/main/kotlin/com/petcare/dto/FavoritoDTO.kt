package com.petcare.dto

/*
 * Comentario de modulo PetCare:
 * Objeto de transferencia. Define la forma de los datos que entran o salen por la API.
 */

import com.fasterxml.jackson.annotation.JsonProperty
import com.petcare.model.Favorite
import io.swagger.v3.oas.annotations.media.Schema

data class FavoritoDTO(
    @Schema(example = "8") val id: Int? = null,
    @JsonProperty("usuario_id") @Schema(example = "17") val usuarioId: Int = 0,
    @JsonProperty("cuidador_id") @Schema(example = "22") val caregiverId: Int? = null,
    @JsonProperty("mascota_id") @Schema(example = "4") val petId: Int? = null,
    @JsonProperty("fecha_agregado") @Schema(example = "2026-07-15T09:00:00Z") val addedAt: String? = null
) {
    fun toEntity(): Favorite = Favorite(
        usuarioId = usuarioId,
        caregiverId = caregiverId,
        petId = petId
    )

    companion object {
        fun fromEntity(entity: Favorite) = FavoritoDTO(
            id = entity.id,
            usuarioId = entity.usuarioId ?: 0,
            caregiverId = entity.caregiverId,
            petId = entity.petId,
            addedAt = entity.addedAt?.toString()
        )
    }
}
