package com.petcare.controller

/*
 * Comentario de modulo PetCare:
 * Controlador REST. Recibe peticiones HTTP, valida el flujo basico y delega la logica al servicio.
 */

import com.petcare.dto.NotaUsuarioDTO
import com.petcare.service.NotaService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/notas")
@Tag(name = "Notas", description = "Notas privadas de un propietario sobre cuidadores u otros objetivos")
class NotaController(private val service: NotaService) {

    @Operation(summary = "Listar las notas de un propietario")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Lista de notas del propietario")
    ])
    @GetMapping
    fun listar(@RequestParam propietarioId: Int): ResponseEntity<List<NotaUsuarioDTO>> =
        ResponseEntity.ok(service.listar(propietarioId).map { NotaUsuarioDTO.fromEntity(it) })

    @Operation(summary = "Crear una nota")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Nota creada"),
        ApiResponse(responseCode = "400", description = "propietario_id, objetivo_id o nota faltantes")
    ])
    @PostMapping
    fun crear(@RequestBody request: NotaUsuarioDTO): ResponseEntity<*> {
        if (request.ownerId <= 0 || request.targetId <= 0 || request.nota.isBlank()) {
            return ResponseEntity.badRequest()
                .body(mapOf("error" to "propietario_id, objetivo_id y nota son requeridos"))
        }
        val saved = service.crear(request.toEntity())
        return ResponseEntity.status(201).body(NotaUsuarioDTO.fromEntity(saved))
    }

    @Operation(summary = "Actualizar el texto de una nota")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Nota actualizada"),
        ApiResponse(responseCode = "400", description = "El texto de la nota es requerido"),
        ApiResponse(responseCode = "404", description = "Nota no encontrada")
    ])
    @PutMapping("/{id}")
    fun actualizar(@PathVariable id: Int, @RequestBody request: NotaUsuarioDTO): ResponseEntity<*> {
        if (request.nota.isBlank()) {
            return ResponseEntity.badRequest().body(mapOf("error" to "nota es requerida"))
        }
        val updated = service.actualizar(id, request.nota)
            ?: return ResponseEntity.status(404).body(mapOf("error" to "Nota no encontrada"))
        return ResponseEntity.ok(NotaUsuarioDTO.fromEntity(updated))
    }

    @Operation(summary = "Eliminar una nota")
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "Nota eliminada"),
        ApiResponse(responseCode = "404", description = "Nota no encontrada")
    ])
    @DeleteMapping("/{id}")
    fun eliminar(@PathVariable id: Int): ResponseEntity<*> {
        if (service.buscar(id).isEmpty) {
            return ResponseEntity.status(404).body(mapOf("error" to "Nota no encontrada"))
        }
        service.eliminar(id)
        return ResponseEntity.noContent().build<Any>()
    }
}
