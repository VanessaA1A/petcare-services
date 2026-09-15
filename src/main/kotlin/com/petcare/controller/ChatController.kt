package com.petcare.controller

/*
 * Comentario de modulo PetCare:
 * Controlador REST. Recibe peticiones HTTP, valida el flujo basico y delega la logica al servicio.
 */

import com.petcare.dto.ChatMessageDTO
import com.petcare.dto.UnreadCountDTO
import com.petcare.exception.StorageException
import com.petcare.model.ChatMessage
import com.petcare.service.ChatService
import com.petcare.service.FileStorageService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.net.MalformedURLException
import java.nio.file.Path

@RestController
@RequestMapping("/api/chat")
@Tag(name = "Chat", description = "Chat interno entre propietario y cuidador, ligado a una solicitud de servicio")
class ChatController(
    private val service: ChatService,
    private val fileStorageService: FileStorageService
) {

    @Operation(summary = "Enviar un mensaje de chat", description = "Persiste el mensaje y lo empuja por WebSocket al receptor si esta conectado.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Mensaje enviado"),
        ApiResponse(responseCode = "400", description = "service_request_id, sender_id, receiver_id o message faltantes")
    ])
    @PostMapping("/mensajes")
    fun enviar(@RequestBody request: ChatMessageDTO): ResponseEntity<*> {
        if (request.serviceRequestId <= 0 || request.senderId <= 0 || request.receiverId <= 0 || request.message.isBlank()) {
            return ResponseEntity.badRequest().body(mapOf("error" to "service_request_id, sender_id, receiver_id y message son requeridos"))
        }
        val saved = service.enviar(request.toEntity())
        return ResponseEntity.status(201).body(ChatMessageDTO.fromEntity(saved))
    }

    @Operation(summary = "Historial de mensajes de una solicitud de servicio")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Historial de mensajes")
    ])
    @GetMapping("/mensajes/{serviceRequestId}")
    fun conversacion(@PathVariable serviceRequestId: Int): ResponseEntity<List<ChatMessageDTO>> =
        ResponseEntity.ok(service.conversacion(serviceRequestId).map { ChatMessageDTO.fromEntity(it) })

    @Operation(summary = "Marcar como leidos los mensajes de una conversacion")
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "Mensajes marcados como leidos")
    ])
    @PutMapping("/mensajes/leidos")
    fun marcarLeidos(@RequestParam serviceRequestId: Int, @RequestParam userId: Int): ResponseEntity<*> {
        service.marcarLeidos(serviceRequestId, userId)
        return ResponseEntity.noContent().build<Any>()
    }

    @Operation(summary = "Cantidad de mensajes no leidos de un usuario")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Cantidad de mensajes no leidos")
    ])
    @GetMapping("/no-leidos/{userId}")
    fun noLeidos(@PathVariable userId: Int): ResponseEntity<UnreadCountDTO> =
        ResponseEntity.ok(UnreadCountDTO(service.noLeidos(userId)))

    @Operation(summary = "Enviar una imagen en el chat", description = "Sube la imagen, la guarda como mensaje (con texto opcional) y la empuja por WebSocket al receptor.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Imagen enviada"),
        ApiResponse(responseCode = "400", description = "senderId/receiverId faltantes, archivo vacio o tipo no permitido")
    ])
    @PostMapping("/{serviceRequestId}/imagen", consumes = ["multipart/form-data"])
    fun enviarImagen(
        @PathVariable serviceRequestId: Int,
        @RequestParam senderId: Int,
        @RequestParam receiverId: Int,
        @RequestParam(required = false, defaultValue = "") message: String,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<*> {
        if (senderId <= 0 || receiverId <= 0) {
            return ResponseEntity.badRequest().body(mapOf("error" to "senderId y receiverId son requeridos"))
        }
        return try {
            val filename = fileStorageService.storeChatImage(serviceRequestId, senderId, file)
            val entity = ChatMessage(
                serviceRequestId = serviceRequestId,
                senderId = senderId,
                receiverId = receiverId,
                message = message,
                imageUrl = "/api/chat/imagen/$filename"
            )
            val saved = service.enviar(entity)
            ResponseEntity.status(201).body(ChatMessageDTO.fromEntity(saved))
        } catch (ex: StorageException) {
            ResponseEntity.badRequest().body(mapOf("error" to ex.message))
        }
    }

    @Operation(summary = "Listar las imagenes compartidas en una conversacion")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Imagenes de la conversacion")
    ])
    @GetMapping("/{serviceRequestId}/imagenes")
    fun imagenes(@PathVariable serviceRequestId: Int): ResponseEntity<List<ChatMessageDTO>> =
        ResponseEntity.ok(service.imagenes(serviceRequestId).map { ChatMessageDTO.fromEntity(it) })

    @Operation(summary = "Servir una imagen de chat por nombre de archivo")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Imagen servida"),
        ApiResponse(responseCode = "404", description = "Imagen no encontrada")
    ])
    @GetMapping("/imagen/{filename}")
    fun servirImagen(@PathVariable filename: String): ResponseEntity<*> {
        return try {
            val path: Path = fileStorageService.loadChatImage(filename)
            val resource: Resource = UrlResource(path.toUri())
            val contentType = when (path.toString().substringAfterLast('.', "jpg").lowercase()) {
                "png" -> MediaType.IMAGE_PNG
                "gif" -> MediaType.IMAGE_GIF
                "webp" -> MediaType.valueOf("image/webp")
                else -> MediaType.IMAGE_JPEG
            }
            ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"${path.fileName}\"")
                .body(resource)
        } catch (ex: StorageException) {
            ResponseEntity.status(404).body(mapOf("error" to ex.message))
        } catch (ex: MalformedURLException) {
            ResponseEntity.status(404).body(mapOf("error" to "Imagen no encontrada"))
        }
    }
}
