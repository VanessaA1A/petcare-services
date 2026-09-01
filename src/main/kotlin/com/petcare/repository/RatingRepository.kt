package com.petcare.repository

/*
 * Comentario de modulo PetCare:
 * Repositorio de persistencia. Expone consultas a PostgreSQL mediante Spring Data.
 */

import com.petcare.model.Rating
import org.springframework.data.jpa.repository.JpaRepository

interface RatingRepository : JpaRepository<Rating, Int> {
    fun findByServiceRequestIdAndRatedByRole(serviceRequestId: Int, ratedByRole: String): Rating?
    fun findByCaregiverIdAndRatedByRole(caregiverId: Int, ratedByRole: String): List<Rating>
    fun findByCaregiverIdAndRatedByRoleOrderByCreatedAtDesc(caregiverId: Int, ratedByRole: String): List<Rating>
    fun findByOwnerIdAndRatedByRole(ownerId: Int, ratedByRole: String): List<Rating>
    fun findByOwnerIdAndRatedByRoleOrderByCreatedAtDesc(ownerId: Int, ratedByRole: String): List<Rating>
}
