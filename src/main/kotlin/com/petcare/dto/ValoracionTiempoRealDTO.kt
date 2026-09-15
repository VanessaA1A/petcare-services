package com.petcare.dto

/*
 * Comentario de modulo PetCare:
 * Objeto de transferencia. Define la forma de los datos que entran o salen por la API.
 */

import com.fasterxml.jackson.annotation.JsonProperty
import com.petcare.model.ValoracionTiempoReal
import io.swagger.v3.oas.annotations.media.Schema

data class ValoracionTiempoRealDTO(
    @Schema(example = "34") val id: Int? = null,
    @JsonProperty("service_request_id") @Schema(example = "1001") val serviceRequestId: Int,
    @JsonProperty("usuario_id") @Schema(example = "22") val usuarioId: Int,
    @JsonProperty("tipo_reaccion") @Schema(example = "CORAZON", description = "CORAZON, ESTRELLA o PULGAR") val tipoReaccion: String,
    @JsonProperty("created_at") @Schema(example = "2026-09-14T14:30:00Z") val createdAt: String? = null
) {
    companion object {
        val TIPOS_VALIDOS = setOf("CORAZON", "ESTRELLA", "PULGAR")

        fun fromEntity(entity: ValoracionTiempoReal) = ValoracionTiempoRealDTO(
            id = entity.id,
            serviceRequestId = entity.serviceRequestId ?: 0,
            usuarioId = entity.usuarioId ?: 0,
            tipoReaccion = entity.tipoReaccion,
            createdAt = entity.createdAt?.toString()
        )
    }

    fun toEntity(): ValoracionTiempoReal = ValoracionTiempoReal(
        serviceRequestId = serviceRequestId,
        usuarioId = usuarioId,
        tipoReaccion = tipoReaccion
    )
}
