package com.petcare.repository

import com.petcare.model.ServiceRequest
import org.springframework.data.jpa.repository.JpaRepository

interface ServiceRequestRepository : JpaRepository<ServiceRequest, Int> {
    fun findByOwnerIdOrderByCreatedAtDesc(ownerId: Int): List<ServiceRequest>
    fun findByStatusOrderByCreatedAtDesc(status: String): List<ServiceRequest>
    fun findByStatusAndSourceTypeOrderByCreatedAtDesc(status: String, sourceType: String): List<ServiceRequest>
    fun findByStatusIgnoreCaseAndSourceTypeIgnoreCaseOrderByCreatedAtDesc(status: String, sourceType: String): List<ServiceRequest>
}
