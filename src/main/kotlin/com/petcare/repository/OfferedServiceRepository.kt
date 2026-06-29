package com.petcare.repository

import com.petcare.model.OfferedService
import org.springframework.data.jpa.repository.JpaRepository

interface OfferedServiceRepository : JpaRepository<OfferedService, Int> {
    fun findByCaregiverIdOrderByCreatedAtDesc(caregiverId: Int): List<OfferedService>
    fun findByIsAvailableTrueOrderByCreatedAtDesc(): List<OfferedService>
}
