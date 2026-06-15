package com.petcare.service

import com.petcare.model.Session
import com.petcare.model.User
import com.petcare.repository.SessionRepository
import com.petcare.repository.UserRepository
import com.petcare.util.HashUtil
import org.springframework.stereotype.Service
import java.util.*

@Service
class AuthService(private val userRepository: UserRepository, private val sessionRepository: SessionRepository) {

    fun authenticate(email: String, password: String): Optional<User> {
        val hashed = HashUtil.md5(password)
        return userRepository.findByEmail(email).filter { it.passwordHash == hashed }
    }

    fun createSession(userId: UUID, ip: String?, userAgent: String?): Session {
        val s = Session()
        s.usuarioId = userId
        s.tokenSesion = UUID.randomUUID().toString()
        s.ipAddress = ip
        s.userAgent = userAgent
        return sessionRepository.save(s)
    }

    fun findSessionByToken(token: String) = sessionRepository.findActiveByTokenSesion(token)
}
