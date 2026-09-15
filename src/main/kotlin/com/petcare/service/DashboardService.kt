package com.petcare.service

/*
 * Comentario de modulo PetCare:
 * Servicio de negocio. Contiene reglas de PetCare que no deben vivir directamente en los controladores.
 */

import com.petcare.repository.ServiceRequestRepository
import com.petcare.repository.UserRepository
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

data class DashboardStats(
    val totalUsuarios: Long,
    val serviciosCompletados: Long,
    val solicitudesActivas: Long,
    val cuidadoresActivos: Long,
    val usuariosPorRol: Map<String, Long>,
    val ultimasSolicitudes: List<Map<String, Any?>>
)

data class DashboardActivityPoint(val fecha: String, val cantidad: Long)

/** Bloque 7 (panel de admin): metricas agregadas para el dashboard. Solo lectura. */
@Service
class DashboardService(
    private val userRepository: UserRepository,
    private val requestRepository: ServiceRequestRepository
) {
    private val estadosActivos = setOf("PENDING", "ACCEPTED", "DONE_BY_CAREGIVER")

    fun stats(): DashboardStats {
        val usuarios = userRepository.findAll()
        val solicitudes = requestRepository.findAll()

        val porRol = usuarios.groupingBy { it.rol ?: "sin_rol" }.eachCount()
            .mapValues { it.value.toLong() }

        val ultimas = solicitudes
            .sortedByDescending { it.createdAt }
            .take(10)
            .map {
                mapOf(
                    "id" to it.id,
                    "titulo" to it.title,
                    "estado" to it.status,
                    "fecha" to it.createdAt?.toString()
                )
            }

        return DashboardStats(
            totalUsuarios = usuarios.size.toLong(),
            serviciosCompletados = solicitudes.count { it.status == "COMPLETED" }.toLong(),
            solicitudesActivas = solicitudes.count { it.status in estadosActivos }.toLong(),
            cuidadoresActivos = usuarios.count { it.rol == "gestor" && it.isActive == true }.toLong(),
            usuariosPorRol = porRol,
            ultimasSolicitudes = ultimas
        )
    }

    fun actividad(dias: Int): List<DashboardActivityPoint> {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        val desde = OffsetDateTime.now().minusDays(dias.toLong() - 1).toLocalDate()
        val solicitudes = requestRepository.findAll()

        val porDia = solicitudes
            .mapNotNull { it.createdAt?.toLocalDate() }
            .filter { !it.isBefore(desde) }
            .groupingBy { it }
            .eachCount()

        return (0 until dias).map { offset ->
            val dia = desde.plusDays(offset.toLong())
            DashboardActivityPoint(dia.format(formatter), (porDia[dia] ?: 0).toLong())
        }
    }
}
