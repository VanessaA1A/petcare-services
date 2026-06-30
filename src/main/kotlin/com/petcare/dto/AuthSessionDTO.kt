package com.petcare.dto

/*
 * Comentario de modulo PetCare:
 * Objeto de transferencia. Define la forma de los datos que entran o salen por la API.
 */

import com.petcare.model.Session

data class AuthSessionDTO(
    val id: Int,
    val tokenSesion: String
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
