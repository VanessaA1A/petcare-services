package com.petcare.dto

/*
 * Comentario de modulo PetCare:
 * Objeto de transferencia. Define la forma de los datos que entran o salen por la API.
 */

import com.fasterxml.jackson.annotation.JsonProperty
import com.petcare.model.ChatMessage
import io.swagger.v3.oas.annotations.media.Schema

data class ChatMessageDTO(
    @Schema(example = "301") val id: Int? = null,
    @JsonProperty("service_request_id") @Schema(example = "1001") val serviceRequestId: Int,
    @JsonProperty("sender_id") @Schema(example = "17") val senderId: Int,
    @JsonProperty("receiver_id") @Schema(example = "22") val receiverId: Int,
    @Schema(example = "Hola, llego en 10 minutos para el paseo.") val message: String,
    @JsonProperty("is_read") @Schema(example = "false") val isRead: Boolean = false,
    @JsonProperty("created_at") @Schema(example = "2026-09-10T14:30:00Z") val createdAt: String? = null
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

data class UnreadCountDTO(@JsonProperty("no_leidos") @Schema(example = "3") val noLeidos: Int)
