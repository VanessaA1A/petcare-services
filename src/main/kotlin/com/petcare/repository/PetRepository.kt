package com.petcare.repository

import com.petcare.model.Pet
import org.springframework.data.jpa.repository.JpaRepository

interface PetRepository : JpaRepository<Pet, Int> {
    fun findByOwnerIdOrderByCreatedAtDesc(ownerId: Int): List<Pet>
}
