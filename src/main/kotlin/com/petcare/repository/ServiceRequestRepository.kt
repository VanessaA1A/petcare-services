package com.petcare.repository

/*
 * Comentario de modulo PetCare:
 * Servicio de negocio. Contiene reglas de PetCare que no deben vivir directamente en los controladores.
 */

import com.petcare.model.ServiceRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ServiceRequestRepository : JpaRepository<ServiceRequest, Int> {
    fun findByOwnerIdOrderByCreatedAtDesc(ownerId: Int): List<ServiceRequest>
    fun findByStatusOrderByCreatedAtDesc(status: String): List<ServiceRequest>
    fun findByStatusAndSourceTypeOrderByCreatedAtDesc(status: String, sourceType: String): List<ServiceRequest>
    fun findByStatusIgnoreCaseAndSourceTypeIgnoreCaseOrderByCreatedAtDesc(status: String, sourceType: String): List<ServiceRequest>

    /**
     * Candidatas cuyo `pet_id` o `pet_ids` (CSV) podrian incluir la mascota buscada. El LIKE es
     * una prefiltracion (puede traer falsos positivos, p. ej. "14" al buscar "4"); quien llama
     * debe confirmar la coincidencia exacta contra `petId`/`petIds.split(",")`.
     */
    @Query(
        "SELECT sr FROM ServiceRequest sr WHERE sr.petId = :petId OR sr.petIds LIKE CONCAT('%', :petId, '%')"
    )
    fun findCandidatesByPetId(@Param("petId") petId: Int): List<ServiceRequest>
}
