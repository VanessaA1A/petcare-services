package com.petcare.service

import com.petcare.model.User
import com.petcare.repository.UserRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserService(private val userRepository: UserRepository) {
    fun create(u: User): User {
        if (u.id == null) u.id = UUID.randomUUID()
        if (u.rol.isNullOrBlank()) u.rol = "gestor"
        return userRepository.save(u)
    }

    fun listAll(): List<User> = userRepository.findAll()
    fun findById(id: UUID): Optional<User> = userRepository.findById(id)
    fun findByEmail(email: String): Optional<User> = userRepository.findByEmail(email)
    fun save(u: User): User {
        if (u.rol.isNullOrBlank()) u.rol = "gestor"
        return userRepository.save(u)
    }
    fun delete(id: UUID) = userRepository.deleteById(id)
}
