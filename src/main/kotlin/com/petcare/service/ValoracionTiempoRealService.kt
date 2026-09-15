package com.petcare.service

/*
 * Comentario de modulo PetCare:
 * Servicio de negocio. Contiene reglas de PetCare que no deben vivir directamente en los controladores.
 */

import com.petcare.model.ValoracionTiempoReal
import com.petcare.repository.ServiceApplicationRepository
import com.petcare.repository.ServiceRequestRepository
import com.petcare.repository.ValoracionTiempoRealRepository
import org.springframework.stereotype.Service

class SolicitudNoEnCursoException(message: String) : RuntimeException(message)

@Service
class ValoracionTiempoRealService(
    private val repository: ValoracionTiempoRealRepository,
    private val requestRepository: ServiceRequestRepository,
    private val applicationRepository: ServiceApplicationRepository,
    private val notificationService: NotificationService
) {
    fun valorar(valoracion: ValoracionTiempoReal): ValoracionTiempoReal {
        val request = requestRepository.findById(valoracion.serviceRequestId ?: -1).orElse(null)
            ?: throw SolicitudNoEnCursoException("Solicitud no encontrada")
        if (request.status != "ACCEPTED") {
            throw SolicitudNoEnCursoException("Solo se puede reaccionar mientras el servicio esta en curso")
        }

        val saved = repository.save(valoracion)

        // Notifica al cuidador asignado (si la reaccion no es de el mismo).
        val caregiverId = applicationRepository.findByServiceRequestIdAndStatus(request.id ?: -1, "ACCEPTED")?.caregiverId
        if (caregiverId != null && caregiverId != saved.usuarioId) {
            notificationService.sendNotificationToUser(
                caregiverId,
                "Nueva reaccion",
                "El propietario reacciono con ${saved.tipoReaccion} en el servicio #${request.id}"
            )
        }

        return saved
    }
}
