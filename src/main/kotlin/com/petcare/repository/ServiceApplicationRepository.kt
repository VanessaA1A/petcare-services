package com.petcare.repository

import com.petcare.model.ServiceApplication
import org.springframework.data.jpa.repository.JpaRepository

interface ServiceApplicationRepository : JpaRepository<ServiceApplication, Int> {
    fun findByCaregiverIdOrderByCreatedAtDesc(caregiverId: Int): List<ServiceApplication>
    fun findByServiceRequestIdInOrderByCreatedAtDesc(serviceRequestIds: Collection<Int>): List<ServiceApplication>
    fun findByServiceRequestIdOrderByCreatedAtDesc(serviceRequestId: Int): List<ServiceApplication>
    fun findByServiceRequestIdAndCaregiverId(serviceRequestId: Int, caregiverId: Int): ServiceApplication?
}
