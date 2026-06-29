package com.petcare.repository

import com.petcare.model.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Int> {
    fun findByEmail(email: String): java.util.Optional<User>
    fun findByUsername(username: String): java.util.Optional<User>
}
