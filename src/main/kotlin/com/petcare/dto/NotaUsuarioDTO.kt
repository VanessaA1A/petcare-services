package com.petcare.dto

/*
 * Comentario de modulo PetCare:
 * Objeto de transferencia. Define la forma de los datos que entran o salen por la API.
 */

import com.fasterxml.jackson.annotation.JsonProperty
import com.petcare.model.UserNote
import io.swagger.v3.oas.annotations.media.Schema

data class NotaUsuarioDTO(
    @Schema(example = "6") val id: Int? = null,
    @JsonProperty("propietario_id") @Schema(example = "17") val ownerId: Int = 0,
    @JsonProperty("objetivo_id") @Schema(example = "22") val targetId: Int = 0,
    @Schema(example = "Muy puntual, se le puede confiar la llave de la casa.") val nota: String = "",
    @JsonProperty("fecha_creacion") @Schema(example = "2026-07-01T10:00:00Z") val createdAt: String? = null,
    @JsonProperty("fecha_actualizacion") @Schema(example = "2026-07-05T16:20:00Z") val updatedAt: String? = null
) {
    fun toEntity(existing: UserNote? = null): UserNote {
        val note = existing ?: UserNote()
        note.ownerId = ownerId
        note.targetId = targetId
        note.nota = nota
        return note
    }

    companion object {
        fun fromEntity(entity: UserNote) = NotaUsuarioDTO(
            id = entity.id,
            ownerId = entity.ownerId ?: 0,
            targetId = entity.targetId ?: 0,
            nota = entity.nota.orEmpty(),
            createdAt = entity.createdAt?.toString(),
            updatedAt = entity.updatedAt?.toString()
        )
    }
}
