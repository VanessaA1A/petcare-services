package com.petcare.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service

@Service
class WsEventService(
    private val registry: WebSocketSessionRegistry,
    private val objectMapper: ObjectMapper
) {

    fun sendToUser(userId: Int, event: WsEvent) {
        val json = objectMapper.writeValueAsString(event)
        registry.sendToUser(userId, json)
    }
}

data class WsEvent(
    val type: String,
    val recipientUserId: Int,
    val title: String,
    val message: String,
    val serviceRequestId: Int? = null,
    val applicationId: Int? = null
)