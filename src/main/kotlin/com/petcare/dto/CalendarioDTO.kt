package com.petcare.dto

/*
 * Comentario de modulo PetCare:
 * Objeto de transferencia. Define la forma de los datos que entran o salen por la API.
 */

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class CalendarioServicioDTO(
    @Schema(example = "2026-09-15") val fecha: String,
    @JsonProperty("solicitud_id") @Schema(example = "1001") val solicitudId: Int,
    @Schema(example = "Paseo de la tarde") val titulo: String,
    @Schema(example = "ACCEPTED") val estado: String,
    @Schema(example = "PROPIETARIO", description = "PROPIETARIO o CUIDADOR, segun el rol del usuario en este servicio") val rol: String
)

data class CalendarioDisponibilidadDTO(
    @Schema(example = "2026-09-15") val fecha: String,
    @Schema(example = "[\"09:00-12:00\", \"14:00-18:00\"]") val horas: List<String>
)

data class CalendarioResponse(
    val servicios: List<CalendarioServicioDTO>,
    val disponibilidad: List<CalendarioDisponibilidadDTO>
)
