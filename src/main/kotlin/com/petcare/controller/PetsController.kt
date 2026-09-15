package com.petcare.controller

/*
 * Comentario de modulo PetCare:
 * Controlador REST. Recibe peticiones HTTP, valida el flujo basico y delega la logica al servicio.
 */

import com.petcare.model.Pet
import com.petcare.repository.UserRepository
import com.petcare.service.PetService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/api/pets")
@Tag(name = "Mascotas", description = "Gestión de mascotas de los propietarios")
class PetsController(
    private val petService: PetService,
    private val userRepository: UserRepository
) {
    /**
     * Regla de negocio: los cuidadores (rol "gestor") no pueden registrar mascotas, para
     * garantizar dedicacion exclusiva a las mascotas de sus clientes. Devuelve un mensaje de
     * error si [ownerId] pertenece a un cuidador, o null si puede registrar mascotas.
     */
    private fun rechazarSiEsCuidador(ownerId: Int): String? {
        val user = userRepository.findById(ownerId).orElse(null) ?: return null
        if (user.rol == "gestor") {
            return "Los cuidadores no pueden registrar mascotas. Si deseas registrar una mascota, cambia tu rol a propietario (solo puedes tener un rol a la vez)."
        }
        return null
    }

    @Operation(summary = "Listar mascotas de un propietario")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Lista de mascotas del propietario")
    ])
    @GetMapping("/owner/{owner_id}")
    fun getByOwner(@PathVariable owner_id: Int): ResponseEntity<*> = ResponseEntity.ok(petService.findByOwnerId(owner_id))

    @Operation(summary = "Obtener una mascota por id")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Mascota encontrada"),
        ApiResponse(responseCode = "404", description = "Mascota no encontrada")
    ])
    @GetMapping("/{id}")
    fun getById(@PathVariable id: Int): ResponseEntity<*> {
        val p = petService.findById(id)
        return if (p.isPresent) ResponseEntity.ok(p.get()) else ResponseEntity.status(404).body(mapOf("error" to "Pet not found"))
    }

    @Operation(summary = "Registrar una mascota")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Mascota creada"),
        ApiResponse(responseCode = "400", description = "owner_id, name, breed o size faltantes/inválidos"),
        ApiResponse(responseCode = "403", description = "El usuario es cuidador y no puede registrar mascotas")
    ])
    @PostMapping
    fun createPet(@RequestBody body: Map<String, Any>): ResponseEntity<*> {
        return try {
            val ownerId = body["owner_id"]?.toString()?.toIntOrNull() ?: throw IllegalArgumentException()
            rechazarSiEsCuidador(ownerId)?.let { return ResponseEntity.status(403).body(mapOf("error" to it)) }
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

    @Operation(summary = "Registrar varias mascotas de un propietario en una sola llamada")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Mascotas creadas"),
        ApiResponse(responseCode = "400", description = "owner_id/pets faltantes o el payload de mascotas es inválido"),
        ApiResponse(responseCode = "403", description = "El usuario es cuidador y no puede registrar mascotas")
    ])
    @PostMapping("/bulk")
    fun createBulk(@RequestBody body: Map<String, Any>): ResponseEntity<*> {
        val owner = body["owner_id"]?.toString()?.toIntOrNull()
            ?: return ResponseEntity.badRequest().body(mapOf("error" to "owner_id and pets are required"))
        rechazarSiEsCuidador(owner)?.let { return ResponseEntity.status(403).body(mapOf("error" to it)) }
        val petsObj = body["pets"] ?: return ResponseEntity.badRequest().body(mapOf("error" to "owner_id and pets are required"))
        return try {
            // Jackson deserializes a JSON array of objects as List<LinkedHashMap<String, Any>>,
            // so this cast is safe at runtime despite type erasure.
            @Suppress("UNCHECKED_CAST")
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

    @Operation(summary = "Actualizar una mascota")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Mascota actualizada"),
        ApiResponse(responseCode = "404", description = "Mascota no encontrada")
    ])
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

    @Operation(summary = "Eliminar una mascota")
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "Mascota eliminada")
    ])
    @DeleteMapping("/{id}")
    fun deletePet(@PathVariable id: Int): ResponseEntity<*> {
        petService.delete(id)
        return ResponseEntity.noContent().build<Any>()
    }

    @Operation(summary = "Listar todas las mascotas")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Lista de todas las mascotas")
    ])
    @GetMapping("/all")
    fun getAll() = ResponseEntity.ok(petService.listAll())
}
