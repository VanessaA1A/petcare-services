package com.petcare.repository

/*
 * Comentario de modulo PetCare:
 * Repositorio de persistencia. Expone consultas a PostgreSQL mediante Spring Data.
 */

import com.petcare.model.ValoracionTiempoReal
import org.springframework.data.jpa.repository.JpaRepository

interface ValoracionTiempoRealRepository : JpaRepository<ValoracionTiempoReal, Int> {
    fun findByServiceRequestIdOrderByCreatedAtDesc(serviceRequestId: Int): List<ValoracionTiempoReal>
}
