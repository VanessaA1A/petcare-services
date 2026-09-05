package com.petcare.service

/*
 * Comentario de modulo PetCare:
 * Servicio de negocio. Contiene reglas de PetCare que no deben vivir directamente en los controladores.
 */

import com.petcare.model.Activity
import com.petcare.repository.ActivityRepository
import org.springframework.stereotype.Service

@Service
class ActivityService(private val activityRepository: ActivityRepository) {
    fun logActivity(sessionId: Int, userId: Int, activityType: String, description: String?, ipAddress: String?): Activity {
        val activity = Activity()
        activity.sesionId = sessionId
        activity.usuarioId = userId
        activity.tipoActividad = activityType
        activity.descripcion = description
        activity.ipAddress = ipAddress
        return activityRepository.save(activity)
    }
}
