package com.petcare.dto

/*
 * Comentario de modulo PetCare:
 * Objeto de transferencia. Define la forma de los datos que entran o salen por la API.
 */

import com.fasterxml.jackson.annotation.JsonProperty
import com.petcare.model.Emergencia
import io.swagger.v3.oas.annotations.media.Schema

data class EmergenciaDTO(
    @Schema(example = "12") val id: Int? = null,
    @JsonProperty("service_request_id") @Schema(example = "1001") val serviceRequestId: Int,
    @JsonProperty("reported_by") @Schema(example = "17") val reportedBy: Int,
    @Schema(example = "MEDICA", description = "MEDICA, ACCIDENTE, MASCOTA_PERDIDA u OTRO") val tipo: String,
    @Schema(example = "La mascota tuvo una reaccion alergica") val descripcion: String? = null,
    @JsonProperty("created_at") @Schema(example = "2026-09-14T14:30:00Z") val createdAt: String? = null
) {
    companion object {
        val TIPOS_VALIDOS = setOf("MEDICA", "ACCIDENTE", "MASCOTA_PERDIDA", "OTRO")

        fun fromEntity(entity: Emergencia) = EmergenciaDTO(
            id = entity.id,
            serviceRequestId = entity.serviceRequestId ?: 0,
            reportedBy = entity.reportedBy ?: 0,
            tipo = entity.tipo,
            descripcion = entity.descripcion,
            createdAt = entity.createdAt?.toString()
        )
    }

    fun toEntity(): Emergencia = Emergencia(
        serviceRequestId = serviceRequestId,
        reportedBy = reportedBy,
        tipo = tipo,
        descripcion = descripcion
    )
}
