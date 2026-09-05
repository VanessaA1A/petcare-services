package com.petcare.controller

/*
 * Comentario de modulo PetCare:
 * Controlador REST. Recibe peticiones HTTP, valida el flujo basico y delega la logica al servicio.
 */

import com.petcare.dto.OfferedServiceDTO
import com.petcare.service.OfferedServiceService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/offered-services")
@Tag(name = "Servicios ofrecidos", description = "Servicios que un cuidador publica para que los dueños los soliciten")
class OfferedServicesController(private val service: OfferedServiceService) {
    @Operation(summary = "Listar servicios ofrecidos por un cuidador")
    @GetMapping("/caregiver/{caregiverId}")
    fun byCaregiver(@PathVariable caregiverId: Int) = ResponseEntity.ok(
        service.byCaregiver(caregiverId).map { OfferedServiceDTO.fromEntity(it) }
    )

    @Operation(summary = "Listar servicios ofrecidos disponibles para todos los dueños")
    @GetMapping("/available")
    fun available() = ResponseEntity.ok(
        service.available().map { OfferedServiceDTO.fromEntity(it) }
    )

    @Operation(summary = "Obtener un servicio ofrecido por id")
    @GetMapping("/{id}")
    fun byId(@PathVariable id: Int): ResponseEntity<*> {
        val item = service.findById(id)
        return if (item.isPresent) ResponseEntity.ok(OfferedServiceDTO.fromEntity(item.get()))
        else ResponseEntity.status(404).body(mapOf("error" to "Offered service not found"))
    }

    @Operation(summary = "Publicar un nuevo servicio ofrecido")
    @PostMapping
    fun create(@RequestBody request: OfferedServiceDTO): ResponseEntity<*> {
        if (request.caregiverId <= 0 || request.serviceTypeId <= 0 || request.title.isBlank()) {
            return ResponseEntity.badRequest().body(mapOf("error" to "caregiverId, serviceTypeId and title are required"))
        }
        val saved = service.save(request.toEntity())
        return ResponseEntity.status(201).body(OfferedServiceDTO.fromEntity(saved))
    }

    @Operation(summary = "Actualizar un servicio ofrecido")
    @PutMapping("/{id}")
    fun update(@PathVariable id: Int, @RequestBody request: OfferedServiceDTO): ResponseEntity<*> {
        val existing = service.findById(id)
        if (existing.isEmpty) return ResponseEntity.status(404).body(mapOf("error" to "Offered service not found"))
        val saved = service.save(request.toEntity(existing.get()))
        return ResponseEntity.ok(OfferedServiceDTO.fromEntity(saved))
    }

    @Operation(summary = "Eliminar un servicio ofrecido")
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Int): ResponseEntity<*> {
        service.delete(id)
        return ResponseEntity.noContent().build<Any>()
    }
}
