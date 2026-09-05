package com.petcare.websocket

/*
 * Comentario de modulo PetCare:
 * Soporte WebSocket. Maneja sesiones y eventos enviados en tiempo real a la app.
 */

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service

@Service
class WsEventService(
    private val registry: WebSocketSessionRegistry,
    private val objectMapper: ObjectMapper
) {

    fun sendToUser(userId: Int, event: WsEvent) {
        // Los controladores construyen el evento; este servicio solo lo serializa y lo envia.
        val json = objectMapper.writeValueAsString(event)
        registry.sendToUser(userId, json)
    }
}

// Evento simple que entiende la app Android para mostrar cambios de servicios.
data class WsEvent(
    val type: String,
    val recipientUserId: Int,
    val title: String,
    val message: String,
    val serviceRequestId: Int? = null,
    val applicationId: Int? = null
)
