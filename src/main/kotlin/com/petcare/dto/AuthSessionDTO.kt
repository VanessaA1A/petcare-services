package com.petcare.dto

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
