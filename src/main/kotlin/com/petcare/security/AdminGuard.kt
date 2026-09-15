package com.petcare.security

/*
 * Comentario de modulo PetCare:
 * Seguridad del backend. Configura autenticacion, JWT y usuarios reconocidos por Spring Security.
 */

import com.petcare.service.AuthService
import com.petcare.service.UserService
import org.springframework.stereotype.Component

/**
 * Chequeo manual de "es un administrador autenticado", reutilizado por los controladores del
 * panel de admin. Este proyecto no usa hasRole()/hasAuthority() en ningun otro lado (todo el
 * resto de @PreAuthorize solo verifica isAuthenticated()), asi que se sigue el mismo patron de
 * lookup manual de token -> sesion -> usuario ya usado en AuthController.me().
 */
@Component
class AdminGuard(
    private val authService: AuthService,
    private val userService: UserService
) {
    /** Devuelve el usuario administrador si el Bearer token es valido y pertenece a un admin, o null. */
    fun adminFromBearer(authorizationHeader: String?): com.petcare.model.User? {
        val token = authorizationHeader?.takeIf { it.startsWith("Bearer ") }?.substring("Bearer ".length)
            ?: return null
        val session = authService.findSessionByToken(token).orElse(null) ?: return null
        val user = userService.findById(session.usuarioId ?: -1).orElse(null) ?: return null
        return user.takeIf { it.rol == "administrador" }
    }
}
