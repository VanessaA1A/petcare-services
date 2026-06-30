package com.petcare.dto

/*
 * Comentario de modulo PetCare:
 * Objeto de transferencia. Define la forma de los datos que entran o salen por la API.
 */

import jakarta.validation.constraints.Size

data class UpdateProfileRequest(
    @field:Size(max = 100)
    val nombre: String?,

    @field:Size(max = 100)
    val apellido: String?,

    @field:Size(max = 30)
    val telefono: String?
)
