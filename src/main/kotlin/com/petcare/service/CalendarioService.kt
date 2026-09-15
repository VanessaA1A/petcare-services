package com.petcare.service

/*
 * Comentario de modulo PetCare:
 * Servicio de negocio. Contiene reglas de PetCare que no deben vivir directamente en los controladores.
 */

import com.petcare.dto.CalendarioDisponibilidadDTO
import com.petcare.dto.CalendarioResponse
import com.petcare.dto.CalendarioServicioDTO
import com.petcare.repository.ServiceApplicationRepository
import com.petcare.repository.ServiceRequestRepository
import org.springframework.stereotype.Service

/**
 * Bloque 9 (calendario integrado). "disponibilidad" queda como lista vacia por ahora: el
 * backend no tiene hoy un concepto de horario de disponibilidad del cuidador (la app Android
 * tiene un `AvailabilityEntity` puramente local en Room, sin endpoint que lo sincronice) -
 * agregar ese concepto es un feature aparte, fuera del alcance de este bloque.
 */
@Service
class CalendarioService(
    private val requestRepository: ServiceRequestRepository,
    private val applicationRepository: ServiceApplicationRepository
) {
    private val estadosProgramados = setOf("ACCEPTED", "DONE_BY_CAREGIVER", "COMPLETED")

    fun obtenerCalendario(usuarioId: Int, mes: Int, anio: Int): CalendarioResponse {
        val prefijo = "%04d-%02d".format(anio, mes)

        val comoPropietario = requestRepository.findByOwnerIdOrderByCreatedAtDesc(usuarioId)
            .filter { it.status in estadosProgramados && it.requestedDate?.startsWith(prefijo) == true }
            .map {
                CalendarioServicioDTO(
                    fecha = it.requestedDate ?: "",
                    solicitudId = it.id ?: 0,
                    titulo = it.title ?: "",
                    estado = it.status,
                    rol = "PROPIETARIO"
                )
            }

        val comoCuidador = applicationRepository.findByCaregiverIdOrderByCreatedAtDesc(usuarioId)
            .filter { it.status in estadosProgramados }
            .mapNotNull { application ->
                val request = requestRepository.findById(application.serviceRequestId ?: -1).orElse(null)
                if (request != null && request.requestedDate?.startsWith(prefijo) == true) {
                    CalendarioServicioDTO(
                        fecha = request.requestedDate ?: "",
                        solicitudId = request.id ?: 0,
                        titulo = request.title ?: "",
                        estado = application.status,
                        rol = "CUIDADOR"
                    )
                } else null
            }

        return CalendarioResponse(
            servicios = (comoPropietario + comoCuidador).sortedBy { it.fecha },
            disponibilidad = emptyList<CalendarioDisponibilidadDTO>()
        )
    }
}
