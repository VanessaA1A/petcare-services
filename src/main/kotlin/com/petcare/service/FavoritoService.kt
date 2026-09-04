package com.petcare.service

/*
 * Comentario de modulo PetCare:
 * Servicio de negocio. Contiene reglas de PetCare que no deben vivir directamente en los controladores.
 */

import com.petcare.model.Favorite
import com.petcare.repository.FavoriteRepository
import org.springframework.stereotype.Service

@Service
class FavoritoService(private val repository: FavoriteRepository) {
    fun listar(usuarioId: Int): List<Favorite> = repository.findByUsuarioId(usuarioId)

    fun agregar(favorito: Favorite): Favorite {
        // Evita duplicar el mismo favorito (cuidador o mascota) para un usuario.
        val existente = when {
            favorito.caregiverId != null ->
                repository.findByUsuarioIdAndCaregiverId(favorito.usuarioId ?: -1, favorito.caregiverId!!).orElse(null)
            favorito.petId != null ->
                repository.findByUsuarioIdAndPetId(favorito.usuarioId ?: -1, favorito.petId!!).orElse(null)
            else -> null
        }
        return existente ?: repository.save(favorito)
    }

    fun existe(id: Int): Boolean = repository.existsById(id)

    fun eliminar(id: Int) = repository.deleteById(id)
}
