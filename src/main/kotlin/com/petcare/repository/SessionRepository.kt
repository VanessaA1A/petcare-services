package com.petcare.repository

import com.petcare.model.Session
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface SessionRepository : JpaRepository<Session, UUID> {
    @Query("SELECT s FROM Session s WHERE s.tokenSesion = :token AND (s.fechaFin IS NULL OR s.logoutExplicito = false)")
    fun findActiveByTokenSesion(@Param("token") token: String): java.util.Optional<Session>
}
