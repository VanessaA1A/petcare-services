package com.petcare.service

/*
 * Comentario de modulo PetCare:
 * Servicio de negocio. Contiene reglas de PetCare que no deben vivir directamente en los controladores.
 */

import com.petcare.repository.RatingRepository
import com.petcare.repository.ServiceApplicationRepository
import com.petcare.repository.ServiceRequestRepository
import com.petcare.repository.UserRepository
import org.springframework.stereotype.Service

/**
 * Calcula y persiste la "etiqueta" (badge) de un usuario segun su historial de servicios y
 * calificaciones. Bloque 7.
 *
 * Nota de alcance: el esquema actual no distingue cancelaciones "justificadas" de
 * "injustificadas" (solo existe el estado CANCELLED, sin motivo estructurado mas alla del
 * texto libre `motivo_cancelacion`), asi que "cancelaciones" aqui cuenta todas las
 * solicitudes/postulaciones canceladas involucrando al usuario.
 */
@Service
class BadgeService(
    private val userRepository: UserRepository,
    private val ratingRepository: RatingRepository,
    private val requestRepository: ServiceRequestRepository,
    private val applicationRepository: ServiceApplicationRepository
) {
    companion object {
        const val NUEVO = "NUEVO"
        const val EN_CRECIMIENTO = "EN_CRECIMIENTO"
        const val CONFIABLE = "CONFIABLE"
        const val EXPERIMENTADO = "EXPERIMENTADO"
        const val ELITE = "ELITE"
        const val EN_OBSERVACION = "EN_OBSERVACION"
    }

    fun calcularBadge(usuarioId: Int): String {
        val user = userRepository.findById(usuarioId).orElse(null) ?: return NUEVO
        val esCuidador = user.rol == "gestor"

        val serviciosCompletados: Int
        val cancelaciones: Int
        val calificacionPromedio: Double

        if (esCuidador) {
            serviciosCompletados = applicationRepository.findByCaregiverIdOrderByCreatedAtDesc(usuarioId)
                .count { it.status == "COMPLETED" }
            cancelaciones = applicationRepository.findByCaregiverIdOrderByCreatedAtDesc(usuarioId)
                .count { it.status == "CANCELLED" }
            val ratings = ratingRepository.findByCaregiverIdAndRatedByRole(usuarioId, "OWNER")
            calificacionPromedio = ratings.map { it.score }.average().takeUnless { it.isNaN() } ?: 5.0
        } else {
            val solicitudes = requestRepository.findByOwnerIdOrderByCreatedAtDesc(usuarioId)
            serviciosCompletados = solicitudes.count { it.status == "COMPLETED" }
            cancelaciones = solicitudes.count { it.status == "CANCELLED" }
            val ratings = ratingRepository.findByOwnerIdAndRatedByRole(usuarioId, "CAREGIVER")
            calificacionPromedio = ratings.map { it.score }.average().takeUnless { it.isNaN() } ?: 5.0
        }

        return when {
            calificacionPromedio < 3.0 || cancelaciones > 3 -> EN_OBSERVACION
            serviciosCompletados >= 60 && calificacionPromedio >= 4.8 && cancelaciones == 0 -> ELITE
            serviciosCompletados >= 30 && calificacionPromedio >= 4.5 -> EXPERIMENTADO
            serviciosCompletados >= 10 && calificacionPromedio >= 4.0 -> CONFIABLE
            serviciosCompletados >= 3 && calificacionPromedio >= 3.5 -> EN_CRECIMIENTO
            else -> NUEVO
        }
    }

    /** Recalcula y persiste el badge de [usuarioId]. Silenciosamente no hace nada si el usuario no existe. */
    fun actualizarBadge(usuarioId: Int) {
        val user = userRepository.findById(usuarioId).orElse(null) ?: return
        user.badge = calcularBadge(usuarioId)
        userRepository.save(user)
    }
}
