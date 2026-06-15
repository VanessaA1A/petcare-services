package com.petcare.service

import com.petcare.model.Pet
import com.petcare.repository.PetRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class PetService(private val petRepository: PetRepository) {
    fun findByOwnerId(ownerId: UUID): List<Pet> = petRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId)
    fun findById(id: UUID) = petRepository.findById(id)
    fun create(p: Pet): Pet { if (p.id == null) p.id = UUID.randomUUID(); return petRepository.save(p) }
    fun bulkCreate(pets: List<Pet>): List<Pet> = petRepository.saveAll(pets)
    fun update(p: Pet): Pet = petRepository.save(p)
    fun delete(id: UUID) = petRepository.deleteById(id)
    fun listAll(): List<Pet> = petRepository.findAll()
}
