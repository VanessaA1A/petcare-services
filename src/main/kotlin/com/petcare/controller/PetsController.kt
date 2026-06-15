package com.petcare.controller

import com.petcare.model.Pet
import com.petcare.service.PetService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

import java.math.BigDecimal
import java.util.*

@RestController
@RequestMapping("/api/pets")
class PetsController(private val petService: PetService) {

    @GetMapping("/owner/{owner_id}")
    fun getByOwner(@PathVariable owner_id: String): ResponseEntity<*> {
        return try {
            val owner = UUID.fromString(owner_id)
            ResponseEntity.ok(petService.findByOwnerId(owner))
        } catch (ex: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to "owner_id is required"))
        }
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: String): ResponseEntity<*> {
        return try {
            val uuid = UUID.fromString(id)
            val p = petService.findById(uuid)
            if (p.isPresent) ResponseEntity.ok(p.get()) else ResponseEntity.status(404).body(mapOf("error" to "Pet not found"))
        } catch (ex: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to "id is required"))
        }
    }

    @PostMapping
    fun createPet(@RequestBody body: Map<String, Any>): ResponseEntity<*> {
        return try {
            val ownerId = UUID.fromString(body["owner_id"]?.toString() ?: throw IllegalArgumentException())
            val name = body["name"] as? String
            val breed = body["breed"] as? String
            val size = body["size"] as? String
            if (name == null || breed == null || size == null) return ResponseEntity.badRequest().body(mapOf("error" to "name, breed and size are required"))
            val pet = Pet()
            pet.ownerId = ownerId
            pet.name = name
            pet.species = body.getOrDefault("species", null) as String?
            pet.breed = breed
            pet.size = size
            if (body.containsKey("age")) pet.age = (body["age"] as Number).toInt()
            if (body.containsKey("weight")) pet.weight = BigDecimal(body["weight"]?.toString() ?: "0")
            pet.description = body.getOrDefault("description", null) as String?
            val saved = petService.create(pet)
            ResponseEntity.status(201).body(saved)
        } catch (ex: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to "owner_id is required"))
        }
    }

    @PostMapping("/bulk")
    fun createBulk(@RequestBody body: Map<String, Any>): ResponseEntity<*> {
        val ownerObj = body["owner_id"] ?: return ResponseEntity.badRequest().body(mapOf("error" to "owner_id and pets are required"))
        val petsObj = body["pets"] ?: return ResponseEntity.badRequest().body(mapOf("error" to "owner_id and pets are required"))
        return try {
            val owner = UUID.fromString(ownerObj.toString())
            val petMaps = petsObj as List<Map<String, Any>>
            val pets = petMaps.map { pm ->
                val p = Pet()
                p.ownerId = owner
                p.name = pm["name"] as String
                p.species = pm.getOrDefault("species", null) as String?
                p.breed = pm["breed"] as String
                p.size = pm["size"] as String
                if (pm.containsKey("age")) p.age = (pm["age"] as Number).toInt()
                if (pm.containsKey("weight")) p.weight = BigDecimal(pm["weight"]?.toString() ?: "0")
                p.description = pm.getOrDefault("description", null) as String?
                p
            }
            val saved = petService.bulkCreate(pets)
            ResponseEntity.status(201).body(saved)
        } catch (ex: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to "owner_id is required"))
        }
    }

    @PutMapping("/{id}")
    fun updatePet(@PathVariable id: String, @RequestBody body: Map<String, Any>): ResponseEntity<*> {
        return try {
            val uuid = UUID.fromString(id)
            val po = petService.findById(uuid)
            if (po.isEmpty) return ResponseEntity.status(404).body(mapOf("error" to "Pet not found"))
            val p = po.get()
            if (body.containsKey("name")) p.name = body["name"] as String
            if (body.containsKey("species")) p.species = body["species"] as String
            if (body.containsKey("breed")) p.breed = body["breed"] as String
            if (body.containsKey("size")) p.size = body["size"] as String
            if (body.containsKey("age")) p.age = (body["age"] as Number).toInt()
            if (body.containsKey("weight")) p.weight = BigDecimal(body["weight"]?.toString() ?: "0")
            if (body.containsKey("description")) p.description = body["description"] as String
            val saved = petService.update(p)
            ResponseEntity.ok(saved)
        } catch (ex: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to "id is required"))
        }
    }

    @DeleteMapping("/{id}")
    fun deletePet(@PathVariable id: String): ResponseEntity<*> {
        return try {
            val uuid = UUID.fromString(id)
            petService.delete(uuid)
            ResponseEntity.noContent().build<Any>()
        } catch (ex: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to "id is required"))
        }
    }

    @GetMapping("/all")
    fun getAll() = ResponseEntity.ok(petService.listAll())
}
