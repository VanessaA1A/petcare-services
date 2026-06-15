package com.petcare.repository

import com.petcare.model.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserRepository : JpaRepository<User, UUID> {
    fun findByEmail(email: String): java.util.Optional<User>
    fun findByUsername(username: String): java.util.Optional<User>
}
