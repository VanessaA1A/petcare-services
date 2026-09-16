package com.petcare.service

/*
 * Comentario de modulo PetCare:
 * Servicio de negocio. Contiene reglas de PetCare que no deben vivir directamente en los controladores.
 */

import com.petcare.dto.CalendarioDisponibilidadDTO
import com.petcare.dto.CalendarioResponse
import com.petcare.dto.CalendarioServicioDTO
import com.petcare.repository.DisponibilidadCuidadorRepository
import com.petcare.repository.ServiceApplicationRepository
import com.petcare.repository.ServiceRequestRepository
import org.springframework.stereotype.Service
import java.time.DateTimeException
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * Bloque 9 (calendario integrado). "disponibilidad" expande el horario semanal recurrente del
 * cuidador (tabla `disponibilidad_cuidador`, configurado via DisponibilidadCuidadorController)
 * en fechas concretas dentro del mes/anio pedido, para que la app pueda pintarlas junto a los
 * servicios agendados. Convencion de `dia_semana`: 0 = lunes ... 6 = domingo (coincide con la
 * grilla Lun-Dom de la pantalla de disponibilidad en la app).
 */
@Service
class CalendarioService(
    private val requestRepository: ServiceRequestRepository,
    private val applicationRepository: ServiceApplicationRepository,
    private val disponibilidadRepository: DisponibilidadCuidadorRepository
) {
    private val estadosProgramados = setOf("ACCEPTED", "DONE_BY_CAREGIVER", "COMPLETED")
    private val formatoHora = DateTimeFormatter.ofPattern("HH:mm")

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
            disponibilidad = expandirDisponibilidad(usuarioId, mes, anio)
        )
    }

    private fun expandirDisponibilidad(cuidadorId: Int, mes: Int, anio: Int): List<CalendarioDisponibilidadDTO> {
        val horarios = disponibilidadRepository.findByCuidadorIdAndActivoTrueOrderByDiaSemanaAscHoraInicioAsc(cuidadorId)
        if (horarios.isEmpty()) return emptyList()

        val yearMonth = try {
            YearMonth.of(anio, mes)
        } catch (e: DateTimeException) {
            return emptyList()
        }
        val porDiaSemana = horarios.groupBy { it.diaSemana }

        return (1..yearMonth.lengthOfMonth()).mapNotNull { dia ->
            val fecha = yearMonth.atDay(dia)
            val diaSemana = fecha.dayOfWeek.value - 1 // lunes=0 ... domingo=6
            val slots = porDiaSemana[diaSemana] ?: return@mapNotNull null
            CalendarioDisponibilidadDTO(
                fecha = fecha.toString(),
                horas = slots.map { "${it.horaInicio.format(formatoHora)}-${it.horaFin.format(formatoHora)}" }
            )
        }
    }
}
