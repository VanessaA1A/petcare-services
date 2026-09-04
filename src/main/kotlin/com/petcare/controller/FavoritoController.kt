package com.petcare.controller

/*
 * Comentario de modulo PetCare:
 * Controlador REST. Recibe peticiones HTTP, valida el flujo basico y delega la logica al servicio.
 */

import com.petcare.dto.FavoritoDTO
import com.petcare.service.FavoritoService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/favoritos")
@Tag(name = "Favoritos", description = "Cuidadores y mascotas marcados como favoritos por un usuario")
class FavoritoController(private val service: FavoritoService) {

    @Operation(summary = "Listar los favoritos de un usuario")
    @GetMapping
    fun listar(@RequestParam usuarioId: Int): ResponseEntity<List<FavoritoDTO>> =
        ResponseEntity.ok(service.listar(usuarioId).map { FavoritoDTO.fromEntity(it) })

    @Operation(summary = "Agregar un favorito", description = "Debe incluir cuidador_id o mascota_id ademas de usuario_id.")
    @PostMapping
    fun agregar(@RequestBody request: FavoritoDTO): ResponseEntity<*> {
        if (request.usuarioId <= 0 || (request.caregiverId == null && request.petId == null)) {
            return ResponseEntity.badRequest()
                .body(mapOf("error" to "usuario_id y (cuidador_id o mascota_id) son requeridos"))
        }
        val saved = service.agregar(request.toEntity())
        return ResponseEntity.status(201).body(FavoritoDTO.fromEntity(saved))
    }

    @Operation(summary = "Eliminar un favorito")
    @DeleteMapping("/{id}")
    fun eliminar(@PathVariable id: Int): ResponseEntity<*> {
        if (!service.existe(id)) {
            return ResponseEntity.status(404).body(mapOf("error" to "Favorito no encontrado"))
        }
        service.eliminar(id)
        return ResponseEntity.noContent().build<Any>()
    }
}
