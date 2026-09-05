package com.petcare.repository

/*
 * Comentario de modulo PetCare:
 * Repositorio de persistencia. Expone consultas a PostgreSQL mediante Spring Data.
 */

import com.petcare.model.Verificacion
import org.springframework.data.jpa.repository.JpaRepository

interface VerificacionRepository : JpaRepository<Verificacion, Int> {
    fun findTopByEmailAndUsadoFalseOrderByCreadoEnDesc(email: String): Verificacion?
}
