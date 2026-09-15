package com.petcare.repository

/*
 * Comentario de modulo PetCare:
 * Repositorio de persistencia. Expone consultas a PostgreSQL mediante Spring Data.
 */

import com.petcare.model.AlertaPerdida
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface AlertaPerdidaRepository : JpaRepository<AlertaPerdida, Int> {
    fun findByEstadoOrderByFechaCreacionDesc(estado: String): List<AlertaPerdida>
    fun findByEstadoAndFechaCreacionBefore(estado: String, antesDe: LocalDateTime): List<AlertaPerdida>
}
