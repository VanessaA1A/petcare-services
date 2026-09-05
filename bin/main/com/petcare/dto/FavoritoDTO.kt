package com.petcare.dto

/*
 * Comentario de modulo PetCare:
 * Objeto de transferencia. Define la forma de los datos que entran o salen por la API.
 */

import com.fasterxml.jackson.annotation.JsonProperty
import com.petcare.model.Favorite

data class FavoritoDTO(
    val id: Int? = null,
    @JsonProperty("usuario_id") val usuarioId: Int = 0,
    @JsonProperty("cuidador_id") val caregiverId: Int? = null,
    @JsonProperty("mascota_id") val petId: Int? = null,
    @JsonProperty("fecha_agregado") val addedAt: String? = null
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
