package com.petcare.dto

/*
 * Comentario de modulo PetCare:
 * Objeto de transferencia. Define la forma de los datos que entran o salen por la API.
 */

data class NearbyServiceRequestDTO(val solicitud: ServiceRequestDTO, val distanciaKm: Double)
data class NearbyOfferedServiceDTO(val oferta: OfferedServiceDTO, val distanciaKm: Double)
data class NearbyCaregiverDTO(
    val id: Int,
    val nombre: String?,
    val distanciaKm: Double
)
