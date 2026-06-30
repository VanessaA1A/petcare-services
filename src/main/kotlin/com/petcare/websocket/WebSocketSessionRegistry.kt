package com.petcare.websocket

/*
 * Comentario de modulo PetCare:
 * Soporte WebSocket. Maneja sesiones y eventos enviados en tiempo real a la app.
 */

import org.springframework.stereotype.Component
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import java.util.concurrent.ConcurrentHashMap

@Component
class WebSocketSessionRegistry {

    private val sessionsByUser = ConcurrentHashMap<Int, MutableSet<WebSocketSession>>()

    fun add(userId: Int, session: WebSocketSession) {
        val userSessions = sessionsByUser.computeIfAbsent(userId) {
            ConcurrentHashMap.newKeySet()
        }
        userSessions.add(session)
    }

    fun remove(session: WebSocketSession) {
        sessionsByUser.values.forEach { sessions ->
            sessions.remove(session)
        }
    }

    fun sendToUser(userId: Int, message: String) {
        val sessions = sessionsByUser[userId].orEmpty()

        sessions
            .filter { it.isOpen }
            .forEach { session ->
                session.sendMessage(TextMessage(message))
            }
    }
}