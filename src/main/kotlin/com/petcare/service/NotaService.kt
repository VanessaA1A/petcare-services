package com.petcare.service

/*
 * Comentario de modulo PetCare:
 * Servicio de negocio. Contiene reglas de PetCare que no deben vivir directamente en los controladores.
 */

import com.petcare.model.UserNote
import com.petcare.repository.UserNoteRepository
import org.springframework.stereotype.Service

@Service
class NotaService(private val repository: UserNoteRepository) {
    fun listar(ownerId: Int): List<UserNote> = repository.findByOwnerId(ownerId)

    fun crear(nota: UserNote): UserNote = repository.save(nota)

    fun buscar(id: Int) = repository.findById(id)

    fun actualizar(id: Int, texto: String): UserNote? {
        val existing = repository.findById(id).orElse(null) ?: return null
        existing.nota = texto
        return repository.save(existing)
    }

    fun eliminar(id: Int) = repository.deleteById(id)
}
