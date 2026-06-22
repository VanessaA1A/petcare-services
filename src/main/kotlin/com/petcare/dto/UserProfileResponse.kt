package com.petcare.dto

import com.petcare.model.User
import java.util.UUID

data class UserProfileResponse(
    val id: UUID,
    val username: String,
    val email: String,
    val rol: String?,
    val nombre: String?,
    val apellido: String?,
    val telefono: String?,
    val fotoPerfilUrl: String?
) {
    companion object {
        fun fromUser(user: User): UserProfileResponse {
            return UserProfileResponse(
                id = user.id!!,
                username = user.username!!,
                email = user.email!!,
                rol = user.normalizedRol,
                nombre = user.nombre,
                apellido = user.apellido,
                telefono = user.telefono,
                fotoPerfilUrl = user.fotoPerfilUrl
            )
        }
    }
}
