package com.petcare.dto

/*
 * Comentario de modulo PetCare:
 * Objeto de transferencia. Define la forma de los datos que entran o salen por la API.
 */

import com.fasterxml.jackson.annotation.JsonProperty
import com.petcare.model.EvidenciaServicio
import io.swagger.v3.oas.annotations.media.Schema

data class EvidenciaServicioDTO(
    @Schema(example = "5") val id: Int? = null,
    @JsonProperty("solicitud_id") @Schema(example = "1001") val solicitudId: Int,
    @Schema(example = "ANTES", description = "ANTES o DESPUES") val tipo: String,
    @JsonProperty("imagen_url") @Schema(example = "/api/solicitudes/evidencia/evidencia_1001_antes_123.jpg") val imagenUrl: String,
    @Schema(example = "Mascota tranquila al llegar") val nota: String? = null,
    @Schema(example = "12.1364") val latitud: Double? = null,
    @Schema(example = "-86.2514") val longitud: Double? = null,
    @Schema(example = "2026-09-15T09:00:00") val fecha: String? = null
) {
    companion object {
        val TIPOS_VALIDOS = setOf("ANTES", "DESPUES")

        fun fromEntity(entity: EvidenciaServicio) = EvidenciaServicioDTO(
            id = entity.id,
            solicitudId = entity.solicitudId ?: 0,
            tipo = entity.tipo,
            imagenUrl = entity.imagenUrl,
            nota = entity.nota,
            latitud = entity.latitud,
            longitud = entity.longitud,
            fecha = entity.fecha?.toString()
        )
    }
}
