package com.petcare.dto

/*
 * Comentario de modulo PetCare:
 * Objeto de transferencia. Define la forma de los datos que entran o salen por la API.
 */

import com.petcare.model.User
import com.petcare.model.Session
import io.swagger.v3.oas.annotations.media.Schema

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
    @Schema(example = "17") val id: Int,
    @Schema(example = "maria.propietaria") val username: String,
    @Schema(example = "maria@petcare.local") val email: String,
    @Schema(example = "propietario") val rol: String?
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
