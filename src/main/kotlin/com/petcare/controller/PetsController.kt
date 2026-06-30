package com.petcare.controller

/*
 * Comentario de modulo PetCare:
 * Controlador REST. Recibe peticiones HTTP, valida el flujo basico y delega la logica al servicio.
 */

import com.petcare.model.Pet
import com.petcare.service.PetService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/api/pets")
class PetsController(private val petService: PetService) {

    @GetMapping("/owner/{owner_id}")
    fun getByOwner(@PathVariable owner_id: Int): ResponseEntity<*> = ResponseEntity.ok(petService.findByOwnerId(owner_id))

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Int): ResponseEntity<*> {
        val p = petService.findById(id)
        return if (p.isPresent) ResponseEntity.ok(p.get()) else ResponseEntity.status(404).body(mapOf("error" to "Pet not found"))
    }

    @PostMapping
    fun createPet(@RequestBody body: Map<String, Any>): ResponseEntity<*> {
        return try {
            val ownerId = body["owner_id"]?.toString()?.toIntOrNull() ?: throw IllegalArgumentException()
            val name = body["name"] as? String
            val breed = body["breed"] as? String
            val size = body["size"] as? String
            if (name == null || breed == null || size == null) return ResponseEntity.badRequest().body(mapOf("error" to "name, breed and size are required"))
            val pet = Pet()
            pet.ownerId = ownerId
            pet.name = name
            pet.species = (body["species"] as? String)?.takeIf { it.isNotBlank() } ?: "Dog"
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
        val owner = body["owner_id"]?.toString()?.toIntOrNull()
            ?: return ResponseEntity.badRequest().body(mapOf("error" to "owner_id and pets are required"))
        val petsObj = body["pets"] ?: return ResponseEntity.badRequest().body(mapOf("error" to "owner_id and pets are required"))
        return try {
            val petMaps = petsObj as List<Map<String, Any>>
            val pets = petMaps.map { pm ->
                val p = Pet()
                p.ownerId = owner
                p.name = pm["name"] as String
                p.species = (pm["species"] as? String)?.takeIf { it.isNotBlank() } ?: "Dog"
                p.breed = pm["breed"] as String
                p.size = pm["size"] as String
                if (pm.containsKey("age")) p.age = (pm["age"] as Number).toInt()
                if (pm.containsKey("weight")) p.weight = BigDecimal(pm["weight"]?.toString() ?: "0")
                p.description = pm.getOrDefault("description", null) as String?
                p
            }
            val saved = petService.bulkCreate(pets)
            ResponseEntity.status(201).body(saved)
        } catch (ex: Exception) {
            ResponseEntity.badRequest().body(mapOf("error" to "invalid pets payload"))
        }
    }

    @PutMapping("/{id}")
    fun updatePet(@PathVariable id: Int, @RequestBody body: Map<String, Any>): ResponseEntity<*> {
        val po = petService.findById(id)
        if (po.isEmpty) return ResponseEntity.status(404).body(mapOf("error" to "Pet not found"))
        val p = po.get()
        if (body.containsKey("name")) p.name = body["name"] as String
        if (body.containsKey("species")) {
            p.species = (body["species"] as? String)?.takeIf { it.isNotBlank() } ?: "Dog"
        }
        if (body.containsKey("breed")) p.breed = body["breed"] as String
        if (body.containsKey("size")) p.size = body["size"] as String
        if (body.containsKey("age")) p.age = (body["age"] as Number).toInt()
        if (body.containsKey("weight")) p.weight = BigDecimal(body["weight"]?.toString() ?: "0")
        if (body.containsKey("description")) p.description = body["description"] as String
        val saved = petService.update(p)
        return ResponseEntity.ok(saved)
    }

    @DeleteMapping("/{id}")
    fun deletePet(@PathVariable id: Int): ResponseEntity<*> {
        petService.delete(id)
        return ResponseEntity.noContent().build<Any>()
    }

    @GetMapping("/all")
    fun getAll() = ResponseEntity.ok(petService.listAll())
}
