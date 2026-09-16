package com.petcare.repository

/*
 * Comentario de modulo PetCare:
 * Repositorio de persistencia. Expone consultas a PostgreSQL mediante Spring Data.
 */

import com.petcare.model.DisponibilidadCuidador
import org.springframework.data.jpa.repository.JpaRepository

interface DisponibilidadCuidadorRepository : JpaRepository<DisponibilidadCuidador, Int> {
    fun findByCuidadorIdAndActivoTrueOrderByDiaSemanaAscHoraInicioAsc(cuidadorId: Int): List<DisponibilidadCuidador>
    fun findByCuidadorIdOrderByDiaSemanaAscHoraInicioAsc(cuidadorId: Int): List<DisponibilidadCuidador>
}
