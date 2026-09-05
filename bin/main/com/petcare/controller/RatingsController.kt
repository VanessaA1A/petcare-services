package com.petcare.controller

/*
 * Comentario de modulo PetCare:
 * Controlador REST. Recibe peticiones HTTP, valida el flujo basico y delega la logica al servicio.
 */

import com.petcare.dto.RatingDTO
import com.petcare.dto.RatingSummaryDTO
import com.petcare.service.RatingService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/ratings")
@Tag(name = "Calificaciones", description = "Calificaciones y reseñas entre dueños y cuidadores")
class RatingsController(private val service: RatingService) {
    @Operation(summary = "Crear o actualizar una calificación", description = "Si el mismo rol ya calificó esa solicitud, se actualiza en vez de duplicar.")
    @PostMapping
    fun create(@RequestBody request: RatingDTO): ResponseEntity<*> {
        if (request.serviceRequestId <= 0 || request.caregiverId <= 0 || request.ownerId <= 0) {
            return ResponseEntity.badRequest().body(mapOf("error" to "serviceRequestId, caregiverId and ownerId are required"))
        }
        return try {
            val saved = service.save(request.toEntity())
            ResponseEntity.status(201).body(RatingDTO.fromEntity(saved))
        } catch (ex: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to ex.message))
        }
    }

    @Operation(summary = "Promedio y cantidad de calificaciones de un cuidador")
    @GetMapping("/caregiver/{caregiverId}/summary")
    fun caregiverSummary(@PathVariable caregiverId: Int): ResponseEntity<RatingSummaryDTO> {
        val (average, count) = service.caregiverSummary(caregiverId)
        return ResponseEntity.ok(RatingSummaryDTO(average, count))
    }

    @Operation(summary = "Listar las reseñas (puntuación y comentario) recibidas por un cuidador")
    @GetMapping("/caregiver/{caregiverId}/reviews")
    fun caregiverReviews(@PathVariable caregiverId: Int): ResponseEntity<List<RatingDTO>> {
        return ResponseEntity.ok(service.caregiverReviews(caregiverId).map { RatingDTO.fromEntity(it) })
    }

    @Operation(summary = "Promedio y cantidad de calificaciones de un propietario")
    @GetMapping("/owner/{ownerId}/summary")
    fun ownerSummary(@PathVariable ownerId: Int): ResponseEntity<RatingSummaryDTO> {
        val (average, count) = service.ownerSummary(ownerId)
        return ResponseEntity.ok(RatingSummaryDTO(average, count))
    }

    @Operation(summary = "Listar las reseñas (puntuación y comentario) recibidas por un propietario")
    @GetMapping("/owner/{ownerId}/reviews")
    fun ownerReviews(@PathVariable ownerId: Int): ResponseEntity<List<RatingDTO>> {
        return ResponseEntity.ok(service.ownerReviews(ownerId).map { RatingDTO.fromEntity(it) })
    }
}
