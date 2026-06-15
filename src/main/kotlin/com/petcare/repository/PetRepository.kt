package com.petcare.repository

import com.petcare.model.Pet
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface PetRepository : JpaRepository<Pet, UUID> {
    fun findByOwnerIdOrderByCreatedAtDesc(ownerId: UUID): List<Pet>
}
