package com.petcare.dto

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
    val id: String,
    val username: String,
    val email: String,
    val rol: String?
) {
    companion object {
        fun fromUser(user: User): UserInfoDTO {
            return UserInfoDTO(
                id = user.id.toString(),
                username = user.username!!,
                email = user.email!!,
                rol = user.normalizedRol
            )
        }
    }
}
