package com.petcare.service

/*
 * Comentario de modulo PetCare:
 * Servicio de negocio. Contiene reglas de PetCare que no deben vivir directamente en los controladores.
 */

import com.petcare.model.AlertaPerdida
import com.petcare.model.Avistamiento
import com.petcare.repository.AlertaPerdidaRepository
import com.petcare.repository.AvistamientoRepository
import com.petcare.repository.PetRepository
import com.petcare.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class AlertaPerdidaService(
    private val repository: AlertaPerdidaRepository,
    private val avistamientoRepository: AvistamientoRepository,
    private val petRepository: PetRepository,
    private val userRepository: UserRepository,
    private val geocodingService: GeocodingService,
    private val notificationService: NotificationService
) {
    private val logger = LoggerFactory.getLogger(AlertaPerdidaService::class.java)

    companion object {
        private const val RADIO_URGENTE_KM = 1.0
        private const val RADIO_AMPLIADO_KM = 5.0
        private const val RADIO_ZONA_KM = 10.0
        private const val DIAS_CIERRE_AUTOMATICO = 7L
    }

    fun activar(alerta: AlertaPerdida): AlertaPerdida {
        val pet = petRepository.findById(alerta.petsId ?: -1).orElse(null)
        if (pet != null && pet.ownerId != alerta.usuarioId) {
            throw NoAutorizadoException("Solo el dueno de la mascota puede reportarla como perdida")
        }

        val saved = repository.save(alerta)
        notificarUsuariosCercanos(saved)
        return saved
    }

    private fun notificarUsuariosCercanos(alerta: AlertaPerdida) {
        val lat = alerta.latitud ?: return
        val lng = alerta.longitud ?: return
        val pet = petRepository.findById(alerta.petsId ?: -1).orElse(null)
        val nombreMascota = pet?.name ?: "una mascota"

        userRepository.findByLatitudIsNotNullAndLongitudIsNotNull()
            .filter { it.id != alerta.usuarioId }
            .forEach { user ->
                val ulat = user.latitud ?: return@forEach
                val ulng = user.longitud ?: return@forEach
                val distancia = geocodingService.calcularDistanciaKm(lat, lng, ulat, ulng)
                val titulo = when {
                    distancia <= RADIO_URGENTE_KM -> "🚨 Mascota perdida muy cerca de ti"
                    distancia <= RADIO_AMPLIADO_KM -> "Mascota perdida cerca de ti"
                    distancia <= RADIO_ZONA_KM -> "Mascota perdida en tu zona"
                    else -> return@forEach
                }
                notificationService.sendNotificationToUser(
                    user.id ?: return@forEach,
                    titulo,
                    "$nombreMascota se perdió cerca de ${alerta.direccionTexto ?: "tu ubicación"}. Ayuda a encontrarla."
                )
            }
    }

    fun reportarAvistamiento(avistamiento: Avistamiento): Avistamiento {
        val alerta = repository.findById(avistamiento.alertaId ?: -1).orElse(null)
            ?: throw MascotaNoEncontradaException("Alerta no encontrada")

        val saved = avistamientoRepository.save(avistamiento)

        alerta.usuarioId?.let { ownerId ->
            if (ownerId != avistamiento.usuarioId) {
                notificationService.sendNotificationToUser(
                    ownerId,
                    "Nuevo avistamiento reportado",
                    "Alguien reportó haber visto a tu mascota"
                )
            }
        }
        return saved
    }

    fun avistamientos(alertaId: Int): List<Avistamiento> = avistamientoRepository.findByAlertaIdOrderByFechaDesc(alertaId)

    fun marcarEncontrada(usuarioId: Int, alertaId: Int): AlertaPerdida? {
        val alerta = repository.findById(alertaId).orElse(null) ?: return null
        if (alerta.usuarioId != usuarioId) {
            throw NoAutorizadoException("Solo el dueno que reporto la alerta puede cerrarla")
        }
        alerta.estado = "ENCONTRADA"
        alerta.fechaCierre = LocalDateTime.now()
        return repository.save(alerta)
    }

    fun cercanas(lat: Double, lng: Double, radioKm: Double): List<Pair<AlertaPerdida, Double>> =
        repository.findByEstadoOrderByFechaCreacionDesc("ACTIVA")
            .mapNotNull { alerta ->
                val alat = alerta.latitud ?: return@mapNotNull null
                val alng = alerta.longitud ?: return@mapNotNull null
                val distancia = geocodingService.calcularDistanciaKm(lat, lng, alat, alng)
                if (distancia > radioKm) null else alerta to distancia
            }
            .sortedBy { it.second }

    /** Bloque 12: cierra automaticamente las alertas activas con mas de 7 dias, a diario a las 8am. */
    @Scheduled(cron = "0 0 8 * * *")
    fun cerrarAlertasVencidas() {
        val limite = LocalDateTime.now().minusDays(DIAS_CIERRE_AUTOMATICO)
        val vencidas = repository.findByEstadoAndFechaCreacionBefore("ACTIVA", limite)
        logger.info("Cierre automatico de alertas de mascota perdida: {} alertas vencidas", vencidas.size)
        vencidas.forEach {
            it.estado = "CERRADA"
            it.fechaCierre = LocalDateTime.now()
            repository.save(it)
        }
    }
}
