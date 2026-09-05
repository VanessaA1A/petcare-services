package com.petcare.dto

/*
 * Comentario de modulo PetCare:
 * Objeto de transferencia. Define la forma de los datos que entran o salen por la API.
 */

import com.petcare.model.User

data class UserProfileResponse(
    val id: Int,
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
