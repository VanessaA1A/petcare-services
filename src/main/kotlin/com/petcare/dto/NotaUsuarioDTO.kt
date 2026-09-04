package com.petcare.dto

/*
 * Comentario de modulo PetCare:
 * Objeto de transferencia. Define la forma de los datos que entran o salen por la API.
 */

import com.fasterxml.jackson.annotation.JsonProperty
import com.petcare.model.UserNote

data class NotaUsuarioDTO(
    val id: Int? = null,
    @JsonProperty("propietario_id") val ownerId: Int = 0,
    @JsonProperty("objetivo_id") val targetId: Int = 0,
    val nota: String = "",
    @JsonProperty("fecha_creacion") val createdAt: String? = null,
    @JsonProperty("fecha_actualizacion") val updatedAt: String? = null
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
