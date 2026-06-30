package com.petcare.service

import com.petcare.model.Rating
import com.petcare.repository.RatingRepository
import com.petcare.repository.ServiceRequestRepository
import org.springframework.stereotype.Service

@Service
class RatingService(
    private val repository: RatingRepository,
    private val requestRepository: ServiceRequestRepository
) {
    fun save(rating: Rating): Rating {
        val request = requestRepository.findById(rating.serviceRequestId ?: -1).orElse(null)
            ?: throw IllegalArgumentException("Service request not found")

        if (request.status != "COMPLETED" && request.status != "ACCEPTED" && request.status != "DONE_BY_CAREGIVER") {
            throw IllegalArgumentException("Solo se puede calificar un servicio confirmado o completado.")
        }

        val existing = repository.findByServiceRequestIdAndRatedByRole(
            rating.serviceRequestId ?: -1,
            rating.ratedByRole
        )
        return repository.save(rating.copy(id = existing?.id ?: rating.id))
    }

    fun caregiverSummary(caregiverId: Int): Pair<Double, Int> {
        val ratings = repository.findByCaregiverIdAndRatedByRole(caregiverId, "OWNER")
        return (ratings.map { it.score }.average().takeUnless { it.isNaN() } ?: 5.0) to ratings.size
    }

    fun ownerSummary(ownerId: Int): Pair<Double, Int> {
        val ratings = repository.findByOwnerIdAndRatedByRole(ownerId, "CAREGIVER")
        return (ratings.map { it.score }.average().takeUnless { it.isNaN() } ?: 5.0) to ratings.size
    }

    fun caregiverReviews(caregiverId: Int): List<Rating> =
        repository.findByCaregiverIdAndRatedByRoleOrderByCreatedAtDesc(caregiverId, "OWNER")
}
