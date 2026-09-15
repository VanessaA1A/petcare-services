package com.petcare.dto

/*
 * Comentario de modulo PetCare:
 * Objeto de transferencia. Define la forma de los datos que entran o salen por la API.
 */

import com.petcare.model.Session
import io.swagger.v3.oas.annotations.media.Schema

data class AuthSessionDTO(
    @Schema(example = "1001") val id: Int,
    @Schema(example = "a1b2c3d4-e5f6-4789-9abc-def012345678") val tokenSesion: String
) {
    companion object {
        fun fromSession(session: Session): AuthSessionDTO {
            return AuthSessionDTO(
                id = session.id!!,
                tokenSesion = session.tokenSesion!!
            )
        }
    }
}
