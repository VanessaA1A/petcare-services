package com.petcare.service

/*
 * Comentario de modulo PetCare:
 * Servicio de negocio. Contiene reglas de PetCare que no deben vivir directamente en los controladores.
 */

import com.petcare.model.User
import com.petcare.repository.UserRepository
import org.springframework.stereotype.Service
import java.util.Optional

@Service
class UserService(private val userRepository: UserRepository) {
    fun create(u: User): User {
        if (u.rol.isNullOrBlank()) u.rol = "gestor"
        return userRepository.save(u)
    }

    fun listAll(): List<User> = userRepository.findAll()
    fun findById(id: Int): Optional<User> = userRepository.findById(id)
    fun findByEmail(email: String): Optional<User> = userRepository.findByEmail(email)
    fun save(u: User): User {
        if (u.rol.isNullOrBlank()) u.rol = "gestor"
        return userRepository.save(u)
    }
    fun delete(id: Int) = userRepository.deleteById(id)
}
