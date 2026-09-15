package com.petcare.repository

/*
 * Comentario de modulo PetCare:
 * Repositorio de persistencia. Expone consultas a PostgreSQL mediante Spring Data.
 */

import com.petcare.model.Emergencia
import org.springframework.data.jpa.repository.JpaRepository

interface EmergenciaRepository : JpaRepository<Emergencia, Int> {
    fun findAllByOrderByCreatedAtDesc(): List<Emergencia>
    fun findByServiceRequestIdOrderByCreatedAtDesc(serviceRequestId: Int): List<Emergencia>
}
