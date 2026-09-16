package com.petcare.service

/*
 * Comentario de modulo PetCare:
 * Servicio de negocio. Contiene reglas de PetCare que no deben vivir directamente en los controladores.
 */

import com.petcare.model.DisponibilidadCuidador
import com.petcare.repository.DisponibilidadCuidadorRepository
import com.petcare.repository.UserRepository
import org.springframework.stereotype.Service
import java.time.LocalTime

class SolapamientoDisponibilidadException(message: String) : RuntimeException(message)

@Service
class DisponibilidadCuidadorService(
    private val repository: DisponibilidadCuidadorRepository,
    private val userRepository: UserRepository
) {
    /** Solo un usuario con rol cuidador ("gestor") puede publicar su disponibilidad. */
    private fun verificarCuidador(usuarioId: Int) {
        val usuario = userRepository.findById(usuarioId).orElse(null)
            ?: throw NoAutorizadoException("Usuario no encontrado")
        if (usuario.rol != "gestor") {
            throw NoAutorizadoException("Solo un cuidador puede configurar su disponibilidad")
        }
    }

    private fun seSolapan(inicioA: LocalTime, finA: LocalTime, inicioB: LocalTime, finB: LocalTime): Boolean =
        inicioA < finB && inicioB < finA

    fun crear(cuidadorId: Int, diaSemana: Int, horaInicio: LocalTime, horaFin: LocalTime): DisponibilidadCuidador {
        verificarCuidador(cuidadorId)
        if (!horaInicio.isBefore(horaFin)) {
            throw SolapamientoDisponibilidadException("La hora de inicio debe ser anterior a la hora de fin")
        }
        val existentes = repository.findByCuidadorIdAndActivoTrueOrderByDiaSemanaAscHoraInicioAsc(cuidadorId)
            .filter { it.diaSemana == diaSemana }
        if (existentes.any { seSolapan(horaInicio, horaFin, it.horaInicio, it.horaFin) }) {
            throw SolapamientoDisponibilidadException("Ya tienes un horario que se solapa ese día")
        }
        return repository.save(
            DisponibilidadCuidador(
                cuidadorId = cuidadorId,
                diaSemana = diaSemana,
                horaInicio = horaInicio,
                horaFin = horaFin
            )
        )
    }

    fun listarPorCuidador(cuidadorId: Int): List<DisponibilidadCuidador> =
        repository.findByCuidadorIdAndActivoTrueOrderByDiaSemanaAscHoraInicioAsc(cuidadorId)

    fun eliminar(id: Int, usuarioId: Int): Boolean {
        val existente = repository.findById(id).orElse(null) ?: return false
        if (existente.cuidadorId != usuarioId) {
            throw NoAutorizadoException("Solo el cuidador dueño de este horario puede eliminarlo")
        }
        repository.delete(existente)
        return true
    }
}
