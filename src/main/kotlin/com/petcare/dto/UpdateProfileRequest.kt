package com.petcare.dto

import jakarta.validation.constraints.Size

data class UpdateProfileRequest(
    @field:Size(max = 100)
    val nombre: String?,

    @field:Size(max = 100)
    val apellido: String?,

    @field:Size(max = 30)
    val telefono: String?
)
