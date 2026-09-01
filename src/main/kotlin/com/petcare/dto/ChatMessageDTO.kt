package com.petcare.dto

/*
 * Comentario de modulo PetCare:
 * Objeto de transferencia. Define la forma de los datos que entran o salen por la API.
 */

import com.fasterxml.jackson.annotation.JsonProperty
import com.petcare.model.ChatMessage

data class ChatMessageDTO(
    val id: Int? = null,
    @JsonProperty("service_request_id") val serviceRequestId: Int,
    @JsonProperty("sender_id") val senderId: Int,
    @JsonProperty("receiver_id") val receiverId: Int,
    val message: String,
    @JsonProperty("is_read") val isRead: Boolean = false,
    @JsonProperty("created_at") val createdAt: String? = null
) {
    fun toEntity(): ChatMessage = ChatMessage(
        serviceRequestId = serviceRequestId,
        senderId = senderId,
        receiverId = receiverId,
        message = message
    )

    companion object {
        fun fromEntity(entity: ChatMessage) = ChatMessageDTO(
            id = entity.id,
            serviceRequestId = entity.serviceRequestId ?: 0,
            senderId = entity.senderId ?: 0,
            receiverId = entity.receiverId ?: 0,
            message = entity.message,
            isRead = entity.isRead,
            createdAt = entity.createdAt?.toString()
        )
    }
}

data class UnreadCountDTO(@JsonProperty("no_leidos") val noLeidos: Int)
