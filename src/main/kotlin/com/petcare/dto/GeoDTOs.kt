package com.petcare.dto

/*
 * Comentario de modulo PetCare:
 * Objeto de transferencia. Define la forma de los datos que entran o salen por la API.
 */

import io.swagger.v3.oas.annotations.media.Schema

data class NearbyServiceRequestDTO(
    val solicitud: ServiceRequestDTO,
    @Schema(example = "2.3") val distanciaKm: Double
)
data class NearbyOfferedServiceDTO(
    val oferta: OfferedServiceDTO,
    @Schema(example = "1.7") val distanciaKm: Double
)
data class NearbyCaregiverDTO(
    @Schema(example = "22") val id: Int,
    @Schema(example = "Carlos Cuidador") val nombre: String?,
    @Schema(example = "0.9") val distanciaKm: Double
)
