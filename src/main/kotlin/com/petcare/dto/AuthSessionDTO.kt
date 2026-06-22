package com.petcare.dto

import com.petcare.model.Session
import java.util.UUID

data class AuthSessionDTO(
    val id: UUID,
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
