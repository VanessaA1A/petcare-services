package com.petcare.service

import com.petcare.model.Pet
import com.petcare.repository.PetRepository
import org.springframework.stereotype.Service

@Service
class PetService(private val petRepository: PetRepository) {
    fun findByOwnerId(ownerId: Int): List<Pet> = petRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId)
    fun findById(id: Int) = petRepository.findById(id)
    fun create(p: Pet): Pet = petRepository.save(p)
    fun bulkCreate(pets: List<Pet>): List<Pet> = petRepository.saveAll(pets)
    fun update(p: Pet): Pet = petRepository.save(p)
    fun delete(id: Int) = petRepository.deleteById(id)
    fun listAll(): List<Pet> = petRepository.findAll()
}
