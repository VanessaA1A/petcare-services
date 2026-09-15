package com.petcare.dto

/*
 * Comentario de modulo PetCare:
 * Objeto de transferencia. Define la forma de los datos que entran o salen por la API.
 */

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Size

data class UpdateProfileRequest(
    @field:Size(max = 100)
    @Schema(example = "Maria")
    val nombre: String?,

    @field:Size(max = 100)
    @Schema(example = "Propietaria")
    val apellido: String?,

    @field:Size(max = 30)
    @Schema(example = "+505 8888 1111")
    val telefono: String?
)
