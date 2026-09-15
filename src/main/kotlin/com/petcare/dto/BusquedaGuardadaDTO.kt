package com.petcare.dto

/*
 * Comentario de modulo PetCare:
 * Objeto de transferencia. Define la forma de los datos que entran o salen por la API.
 */

import com.fasterxml.jackson.annotation.JsonProperty
import com.petcare.model.SavedSearch
import io.swagger.v3.oas.annotations.media.Schema

data class BusquedaGuardadaDTO(
    @Schema(example = "5") val id: Int? = null,
    @JsonProperty("usuario_id") @Schema(example = "17") val usuarioId: Int = 0,
    @Schema(example = "Paseadores cerca de mi casa") val nombre: String = "",
    @JsonProperty("filtros_json") @Schema(example = "{\"tipo\":\"paseo\",\"radioKm\":5}") val filtrosJson: String = "",
    @JsonProperty("fecha_creacion") @Schema(example = "2026-08-01T10:15:00Z") val createdAt: String? = null
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
