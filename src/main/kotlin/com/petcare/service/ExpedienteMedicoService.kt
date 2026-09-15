package com.petcare.service

/*
 * Comentario de modulo PetCare:
 * Servicio de negocio. Contiene reglas de PetCare que no deben vivir directamente en los controladores.
 */

import com.petcare.model.ExpedienteMedico
import com.petcare.repository.ExpedienteMedicoRepository
import com.petcare.repository.PetRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDate

class NoAutorizadoException(message: String) : RuntimeException(message)
class MascotaNoEncontradaException(message: String) : RuntimeException(message)

@Service
class ExpedienteMedicoService(
    private val repository: ExpedienteMedicoRepository,
    private val petRepository: PetRepository,
    private val notificationService: NotificationService
) {
    private val logger = LoggerFactory.getLogger(ExpedienteMedicoService::class.java)

    fun listar(petsId: Int): List<ExpedienteMedico> = repository.findByPetsIdOrderByFechaDesc(petsId)

    /** Solo el dueno de la mascota puede crear/editar/eliminar entradas del expediente. */
    private fun verificarDueno(petsId: Int, usuarioId: Int) {
        val pet = petRepository.findById(petsId).orElse(null)
            ?: throw MascotaNoEncontradaException("Mascota no encontrada")
        if (pet.ownerId != usuarioId) {
            throw NoAutorizadoException("Solo el dueno de la mascota puede modificar su expediente medico")
        }
    }

    fun crear(usuarioId: Int, entrada: ExpedienteMedico): ExpedienteMedico {
        verificarDueno(entrada.petsId ?: -1, usuarioId)
        return repository.save(entrada)
    }

    fun actualizar(usuarioId: Int, id: Int, cambios: ExpedienteMedico): ExpedienteMedico? {
        val existente = repository.findById(id).orElse(null) ?: return null
        verificarDueno(existente.petsId ?: -1, usuarioId)
        cambios.id = existente.id
        cambios.petsId = existente.petsId
        cambios.fechaCreacion = existente.fechaCreacion
        return repository.save(cambios)
    }

    fun eliminar(usuarioId: Int, id: Int): Boolean {
        val existente = repository.findById(id).orElse(null) ?: return false
        verificarDueno(existente.petsId ?: -1, usuarioId)
        repository.delete(existente)
        return true
    }

    /**
     * Bloque 11.2: revisa diariamente las entradas cuya fecha_proxima cae en los proximos 15
     * dias y notifica por push al dueno de la mascota. Corre a las 8am hora del servidor.
     */
    @Scheduled(cron = "0 0 8 * * *")
    fun revisarVacunasProximas() {
        val hoy = LocalDate.now()
        val limite = hoy.plusDays(15)
        val proximas = repository.findByFechaProximaBetween(hoy, limite)
        logger.info("Revision diaria de vacunas proximas: {} entradas encontradas", proximas.size)

        proximas.forEach { entrada ->
            val pet = petRepository.findById(entrada.petsId ?: -1).orElse(null) ?: return@forEach
            val ownerId = pet.ownerId ?: return@forEach
            notificationService.sendNotificationToUser(
                ownerId,
                "Vacuna próxima",
                "${entrada.titulo} de ${pet.name ?: "tu mascota"} vence el ${entrada.fechaProxima}"
            )
        }
    }
}
