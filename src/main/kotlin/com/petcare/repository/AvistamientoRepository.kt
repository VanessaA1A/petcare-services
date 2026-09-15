package com.petcare.repository

/*
 * Comentario de modulo PetCare:
 * Repositorio de persistencia. Expone consultas a PostgreSQL mediante Spring Data.
 */

import com.petcare.model.Avistamiento
import org.springframework.data.jpa.repository.JpaRepository

interface AvistamientoRepository : JpaRepository<Avistamiento, Int> {
    fun findByAlertaIdOrderByFechaDesc(alertaId: Int): List<Avistamiento>
}
