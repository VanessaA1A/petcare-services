package com.petcare.service

/*
 * Comentario de modulo PetCare:
 * Servicio de negocio. Contiene reglas de PetCare que no deben vivir directamente en los controladores.
 */

import com.petcare.model.SavedSearch
import com.petcare.repository.SavedSearchRepository
import org.springframework.stereotype.Service

@Service
class BusquedaGuardadaService(private val repository: SavedSearchRepository) {
    fun listar(usuarioId: Int): List<SavedSearch> = repository.findByUsuarioId(usuarioId)

    fun guardar(busqueda: SavedSearch): SavedSearch = repository.save(busqueda)

    fun existe(id: Int): Boolean = repository.existsById(id)

    fun eliminar(id: Int) = repository.deleteById(id)
}
