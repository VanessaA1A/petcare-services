package com.petcare.repository

/*
 * Comentario de modulo PetCare:
 * Servicio de negocio. Contiene reglas de PetCare que no deben vivir directamente en los controladores.
 */

import com.petcare.model.OfferedService
import org.springframework.data.jpa.repository.JpaRepository

interface OfferedServiceRepository : JpaRepository<OfferedService, Int> {
    fun findByCaregiverIdOrderByCreatedAtDesc(caregiverId: Int): List<OfferedService>
    fun findByIsAvailableTrueOrderByCreatedAtDesc(): List<OfferedService>
}
