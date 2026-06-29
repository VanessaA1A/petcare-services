package com.petcare.controller

import com.petcare.dto.RatingDTO
import com.petcare.dto.RatingSummaryDTO
import com.petcare.service.RatingService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/ratings")
class RatingsController(private val service: RatingService) {
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

    @GetMapping("/caregiver/{caregiverId}/summary")
    fun caregiverSummary(@PathVariable caregiverId: Int): ResponseEntity<RatingSummaryDTO> {
        val (average, count) = service.caregiverSummary(caregiverId)
        return ResponseEntity.ok(RatingSummaryDTO(average, count))
    }

    @GetMapping("/caregiver/{caregiverId}/reviews")
    fun caregiverReviews(@PathVariable caregiverId: Int): ResponseEntity<List<RatingDTO>> {
        return ResponseEntity.ok(service.caregiverReviews(caregiverId).map { RatingDTO.fromEntity(it) })
    }

    @GetMapping("/owner/{ownerId}/summary")
    fun ownerSummary(@PathVariable ownerId: Int): ResponseEntity<RatingSummaryDTO> {
        val (average, count) = service.ownerSummary(ownerId)
        return ResponseEntity.ok(RatingSummaryDTO(average, count))
    }
}
