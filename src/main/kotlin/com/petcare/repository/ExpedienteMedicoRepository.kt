package com.petcare.repository

/*
 * Comentario de modulo PetCare:
 * Repositorio de persistencia. Expone consultas a PostgreSQL mediante Spring Data.
 */

import com.petcare.model.ExpedienteMedico
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface ExpedienteMedicoRepository : JpaRepository<ExpedienteMedico, Int> {
    fun findByPetsIdOrderByFechaDesc(petsId: Int): List<ExpedienteMedico>
    fun findByFechaProximaBetween(desde: LocalDate, hasta: LocalDate): List<ExpedienteMedico>
}
