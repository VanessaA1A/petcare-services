package com.petcare.service

/*
 * Comentario de modulo PetCare:
 * Servicio de negocio. Contiene reglas de PetCare que no deben vivir directamente en los controladores.
 */

import com.petcare.model.Emergencia
import com.petcare.repository.EmergenciaRepository
import com.petcare.repository.ServiceApplicationRepository
import com.petcare.repository.ServiceRequestRepository
import com.petcare.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class EmergenciaService(
    private val repository: EmergenciaRepository,
    private val requestRepository: ServiceRequestRepository,
    private val applicationRepository: ServiceApplicationRepository,
    private val userRepository: UserRepository,
    private val notificationService: NotificationService
) {
    fun reportar(emergencia: Emergencia): Emergencia {
        val saved = repository.save(emergencia)

        val request = requestRepository.findById(saved.serviceRequestId ?: -1).orElse(null)
        val titulo = "Emergencia reportada"
        val cuerpo = "Se reporto una emergencia (${saved.tipo}) en el servicio #${saved.serviceRequestId}"

        // Avisa al dueno y al cuidador asignado del servicio (si no es quien reporto).
        val destinatarios = mutableSetOf<Int>()
        request?.ownerId?.let { destinatarios.add(it) }
        applicationRepository.findByServiceRequestIdAndStatus(saved.serviceRequestId ?: -1, "ACCEPTED")
            ?.caregiverId?.let { destinatarios.add(it) }
        destinatarios.remove(saved.reportedBy)

        destinatarios.forEach { userId ->
            notificationService.sendNotificationToUser(userId, titulo, cuerpo)
        }

        // Avisa tambien a los administradores de la plataforma.
        userRepository.findByRol("administrador").forEach { admin ->
            admin.id?.let { notificationService.sendNotificationToUser(it, titulo, cuerpo) }
        }

        return saved
    }

    fun listar(): List<Emergencia> = repository.findAllByOrderByCreatedAtDesc()
}
