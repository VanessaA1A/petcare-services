package com.petcare.websocket

/*
 * Comentario de modulo PetCare:
 * Soporte WebSocket. Maneja sesiones y eventos enviados en tiempo real a la app.
 */

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
        // El cliente se registra con userId para enviarle mensajes directos.
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
        // Al cerrar la conexion se elimina la sesion para no enviar mensajes a sockets viejos.
        registry.remove(session)
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        // Echo se deja como apoyo para probar el WebSocket desde ws-test.html.
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
