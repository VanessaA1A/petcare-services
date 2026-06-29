package com.petcare.service

import com.petcare.model.Session
import com.petcare.model.User
import com.petcare.repository.SessionRepository
import com.petcare.repository.UserRepository
import com.petcare.util.HashUtil
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.Optional
import java.util.UUID

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository,
    private val passwordEncoder: PasswordEncoder
) {

    fun authenticate(email: String, password: String): Optional<User> {
        return userRepository.findByEmail(email.trim()).filter { user ->
            val hash = user.passwordHash ?: return@filter false
            if (hash.startsWith("$2") && hash.length >= 59 && passwordEncoder.matches(password, hash)) {
                true
            } else {
                val legacyMd5 = HashUtil.md5(password)
                if (hash.equals(legacyMd5, ignoreCase = true)) {
                    user.passwordHash = passwordEncoder.encode(password)
                    userRepository.save(user)
                    true
                } else {
                    false
                }
            }
        }
    }

    fun createSession(userId: Int, tokenSesion: String, ip: String?, userAgent: String?): Session {
        val s = Session()
        s.usuarioId = userId
        s.tokenSesion = tokenSesion
        s.ipAddress = ip
        s.userAgent = userAgent
        return sessionRepository.save(s)
    }

    fun createSession(userId: Int, ip: String?, userAgent: String?): Session {
        return createSession(userId, UUID.randomUUID().toString(), ip, userAgent)
    }

    fun findSessionByToken(token: String) = sessionRepository.findActiveByTokenSesion(token)
}
