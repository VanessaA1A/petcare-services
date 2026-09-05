package com.petcare.dto

/*
 * Comentario de modulo PetCare:
 * Objeto de transferencia. Define la forma de los datos que entran o salen por la API.
 */

import com.petcare.model.User
import com.petcare.model.Session

data class AuthResponseDTO(
    val user: UserInfoDTO,
    val session: AuthSessionDTO
) {
    companion object {
        fun fromUserAndSession(user: User, session: Session): AuthResponseDTO {
            return AuthResponseDTO(
                user = UserInfoDTO.fromUser(user),
                session = AuthSessionDTO.fromSession(session)
            )
        }
    }
}

data class UserInfoDTO(
    val id: Int,
    val username: String,
    val email: String,
    val rol: String?
) {
    companion object {
        fun fromUser(user: User): UserInfoDTO {
            return UserInfoDTO(
                id = user.id!!,
                username = user.username!!,
                email = user.email!!,
                rol = user.normalizedRol
            )
        }
    }
}
