package com.petcare.repository

/*
 * Comentario de modulo PetCare:
 * Repositorio de persistencia. Expone consultas a PostgreSQL mediante Spring Data.
 */

import com.petcare.model.ChatMessage
import org.springframework.data.jpa.repository.JpaRepository

interface ChatMessageRepository : JpaRepository<ChatMessage, Int> {
    fun findByServiceRequestIdOrderByCreatedAtAsc(serviceRequestId: Int): List<ChatMessage>
    fun findByServiceRequestIdAndReceiverIdAndIsReadFalse(serviceRequestId: Int, receiverId: Int): List<ChatMessage>
    fun countByReceiverIdAndIsReadFalse(receiverId: Int): Int
}
