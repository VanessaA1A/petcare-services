package com.petcare.dto

/*
 * Comentario de modulo PetCare:
 * Objeto de transferencia. Define la forma de los datos que entran o salen por la API.
 */

import com.petcare.model.User
import io.swagger.v3.oas.annotations.media.Schema

data class UserProfileResponse(
    @Schema(example = "17") val id: Int,
    @Schema(example = "maria.propietaria") val username: String,
    @Schema(example = "maria@petcare.local") val email: String,
    @Schema(example = "propietario") val rol: String?,
    @Schema(example = "Maria") val nombre: String?,
    @Schema(example = "Propietaria") val apellido: String?,
    @Schema(example = "+505 8888 1111") val telefono: String?,
    @Schema(example = "https://cdn.petcare.local/perfiles/17.jpg") val fotoPerfilUrl: String?,
    @Schema(example = "CONFIABLE", description = "NUEVO, EN_CRECIMIENTO, CONFIABLE, EXPERIMENTADO, ELITE o EN_OBSERVACION") val badge: String?
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
                fotoPerfilUrl = user.fotoPerfilUrl,
                badge = user.badge
            )
        }
    }
}
