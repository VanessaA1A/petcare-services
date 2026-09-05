package com.petcare.dto

/*
 * Comentario de modulo PetCare:
 * Objeto de transferencia. Define la forma de los datos que entran o salen por la API.
 */

import com.fasterxml.jackson.annotation.JsonProperty
import com.petcare.model.SavedSearch

data class BusquedaGuardadaDTO(
    val id: Int? = null,
    @JsonProperty("usuario_id") val usuarioId: Int = 0,
    val nombre: String = "",
    @JsonProperty("filtros_json") val filtrosJson: String = "",
    @JsonProperty("fecha_creacion") val createdAt: String? = null
) {
    fun toEntity(): SavedSearch = SavedSearch(
        usuarioId = usuarioId,
        nombre = nombre,
        filtersJson = filtrosJson
    )

    companion object {
        fun fromEntity(entity: SavedSearch) = BusquedaGuardadaDTO(
            id = entity.id,
            usuarioId = entity.usuarioId ?: 0,
            nombre = entity.nombre.orEmpty(),
            filtrosJson = entity.filtersJson.orEmpty(),
            createdAt = entity.createdAt?.toString()
        )
    }
}
