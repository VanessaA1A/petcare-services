package com.petcare.service

/*
 * Comentario de modulo PetCare:
 * Servicio de negocio. Contiene reglas de PetCare que no deben vivir directamente en los controladores.
 */

import com.petcare.model.OfferedService
import com.petcare.repository.OfferedServiceRepository
import org.springframework.stereotype.Service

@Service
class OfferedServiceService(private val repository: OfferedServiceRepository) {
    fun byCaregiver(caregiverId: Int) = repository.findByCaregiverIdOrderByCreatedAtDesc(caregiverId)
    fun available() = repository.findByIsAvailableTrueOrderByCreatedAtDesc()
    fun findById(id: Int) = repository.findById(id)
    fun save(service: OfferedService) = repository.save(service)
    fun delete(id: Int) = repository.deleteById(id)
}
