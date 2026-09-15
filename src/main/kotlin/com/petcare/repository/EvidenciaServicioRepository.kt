package com.petcare.repository

/*
 * Comentario de modulo PetCare:
 * Repositorio de persistencia. Expone consultas a PostgreSQL mediante Spring Data.
 */

import com.petcare.model.EvidenciaServicio
import org.springframework.data.jpa.repository.JpaRepository

interface EvidenciaServicioRepository : JpaRepository<EvidenciaServicio, Int> {
    fun findBySolicitudIdOrderByFechaAsc(solicitudId: Int): List<EvidenciaServicio>
    fun findBySolicitudIdAndTipo(solicitudId: Int, tipo: String): List<EvidenciaServicio>
}
