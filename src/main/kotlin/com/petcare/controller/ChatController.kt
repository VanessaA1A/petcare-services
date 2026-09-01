package com.petcare.controller

/*
 * Comentario de modulo PetCare:
 * Controlador REST. Recibe peticiones HTTP, valida el flujo basico y delega la logica al servicio.
 */

import com.petcare.dto.ChatMessageDTO
import com.petcare.dto.UnreadCountDTO
import com.petcare.service.ChatService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/chat")
@Tag(name = "Chat", description = "Chat interno entre propietario y cuidador, ligado a una solicitud de servicio")
class ChatController(private val service: ChatService) {

    @Operation(summary = "Enviar un mensaje de chat", description = "Persiste el mensaje y lo empuja por WebSocket al receptor si esta conectado.")
    @PostMapping("/mensajes")
    fun enviar(@RequestBody request: ChatMessageDTO): ResponseEntity<*> {
        if (request.serviceRequestId <= 0 || request.senderId <= 0 || request.receiverId <= 0 || request.message.isBlank()) {
            return ResponseEntity.badRequest().body(mapOf("error" to "service_request_id, sender_id, receiver_id y message son requeridos"))
        }
        val saved = service.enviar(request.toEntity())
        return ResponseEntity.status(201).body(ChatMessageDTO.fromEntity(saved))
    }

    @Operation(summary = "Historial de mensajes de una solicitud de servicio")
    @GetMapping("/mensajes/{serviceRequestId}")
    fun conversacion(@PathVariable serviceRequestId: Int): ResponseEntity<List<ChatMessageDTO>> =
        ResponseEntity.ok(service.conversacion(serviceRequestId).map { ChatMessageDTO.fromEntity(it) })

    @Operation(summary = "Marcar como leidos los mensajes de una conversacion")
    @PutMapping("/mensajes/leidos")
    fun marcarLeidos(@RequestParam serviceRequestId: Int, @RequestParam userId: Int): ResponseEntity<*> {
        service.marcarLeidos(serviceRequestId, userId)
        return ResponseEntity.noContent().build<Any>()
    }

    @Operation(summary = "Cantidad de mensajes no leidos de un usuario")
    @GetMapping("/no-leidos/{userId}")
    fun noLeidos(@PathVariable userId: Int): ResponseEntity<UnreadCountDTO> =
        ResponseEntity.ok(UnreadCountDTO(service.noLeidos(userId)))
}
