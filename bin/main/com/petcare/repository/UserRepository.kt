package com.petcare.repository

/*
 * Comentario de modulo PetCare:
 * Repositorio de persistencia. Expone consultas a PostgreSQL mediante Spring Data.
 */

import com.petcare.model.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Int> {
    fun findByEmail(email: String): java.util.Optional<User>
    fun findByUsername(username: String): java.util.Optional<User>
    fun findByRolAndLatitudIsNotNullAndLongitudIsNotNull(rol: String): List<User>
}
