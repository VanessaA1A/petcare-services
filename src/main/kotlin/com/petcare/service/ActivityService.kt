package com.petcare.service

import com.petcare.model.Activity
import com.petcare.repository.ActivityRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class ActivityService(private val activityRepository: ActivityRepository) {
    fun logActivity(sessionId: UUID, userId: UUID, activityType: String, description: String?, ipAddress: String?): Activity {
        val activity = Activity()
        activity.sesionId = sessionId
        activity.usuarioId = userId
        activity.tipoActividad = activityType
        activity.descripcion = description
        activity.ipAddress = ipAddress
        return activityRepository.save(activity)
    }
}
