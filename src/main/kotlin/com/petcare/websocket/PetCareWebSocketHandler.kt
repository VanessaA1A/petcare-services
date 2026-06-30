package com.petcare.websocket

import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

@Component
class PetCareWebSocketHandler(
    private val registry: WebSocketSessionRegistry
) : TextWebSocketHandler() {

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val userId = session.uri
            ?.query
            ?.split("&")
            ?.mapNotNull {
                val parts = it.split("=")
                if (parts.size == 2) parts[0] to parts[1] else null
            }
            ?.firstOrNull { it.first == "userId" }
            ?.second
            ?.toIntOrNull()

        if (userId == null || userId <= 0) {
            session.close(CloseStatus.BAD_DATA.withReason("userId requerido"))
            return
        }

        session.attributes["userId"] = userId
        registry.add(userId, session)

        session.sendMessage(
            TextMessage(
                """
                {
                  "type": "CONNECTED",
                  "message": "WebSocket conectado",
                  "userId": $userId
                }
                """.trimIndent()
            )
        )
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        registry.remove(session)
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        session.sendMessage(
            TextMessage(
                """
                {
                  "type": "ECHO",
                  "message": ${message.payload.trim().quoteJson()}
                }
                """.trimIndent()
            )
        )
    }

    private fun String.quoteJson(): String {
        return "\"" + this
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n") + "\""
    }
}