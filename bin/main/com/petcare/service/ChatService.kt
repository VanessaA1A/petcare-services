package com.petcare.service

/*
 * Comentario de modulo PetCare:
 * Servicio de negocio. Contiene reglas de PetCare que no deben vivir directamente en los controladores.
 */

import com.petcare.model.ChatMessage
import com.petcare.repository.ChatMessageRepository
import com.petcare.websocket.WsEvent
import com.petcare.websocket.WsEventService
import org.springframework.stereotype.Service

@Service
class ChatService(
    private val repository: ChatMessageRepository,
    private val wsEventService: WsEventService
) {
    fun conversacion(serviceRequestId: Int): List<ChatMessage> =
        repository.findByServiceRequestIdOrderByCreatedAtAsc(serviceRequestId)

    fun enviar(message: ChatMessage): ChatMessage {
        val saved = repository.save(message)
        // Empuja el mensaje en tiempo real si el receptor tiene el WebSocket abierto.
        wsEventService.sendToUser(
            saved.receiverId ?: 0,
            WsEvent(
                type = "CHAT_MESSAGE",
                recipientUserId = saved.receiverId ?: 0,
                title = "Nuevo mensaje",
                message = saved.message,
                serviceRequestId = saved.serviceRequestId
            )
        )
        return saved
    }

    fun marcarLeidos(serviceRequestId: Int, receiverId: Int) {
        val pendientes = repository.findByServiceRequestIdAndReceiverIdAndIsReadFalse(serviceRequestId, receiverId)
        pendientes.forEach { it.isRead = true }
        repository.saveAll(pendientes)
    }

    fun noLeidos(userId: Int): Int = repository.countByReceiverIdAndIsReadFalse(userId)
}
