package com.petcare.model

/*
 * Comentario de modulo PetCare:
 * Entidad de dominio. Representa una tabla o concepto principal usado por la API.
 */

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "chat_messages")
data class ChatMessage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(name = "service_request_id", nullable = false)
    var serviceRequestId: Int? = null,

    @Column(name = "sender_id", nullable = false)
    var senderId: Int? = null,

    @Column(name = "receiver_id", nullable = false)
    var receiverId: Int? = null,

    @Column(columnDefinition = "text", nullable = false)
    var message: String = "",

    @Column(name = "is_read")
    var isRead: Boolean = false,

    @Column(name = "created_at")
    var createdAt: OffsetDateTime? = null
) {
    @PrePersist
    fun prePersist() {
        if (createdAt == null) createdAt = OffsetDateTime.now()
    }
}
