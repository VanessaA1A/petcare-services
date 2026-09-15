package com.petcare.controller

/*
 * Comentario de modulo PetCare:
 * Controlador REST. Recibe peticiones HTTP, valida el flujo basico y delega la logica al servicio.
 */

import com.petcare.dto.FavoritoDTO
import com.petcare.service.FavoritoService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/favoritos")
@Tag(name = "Favoritos", description = "Cuidadores y mascotas marcados como favoritos por un usuario")
class FavoritoController(private val service: FavoritoService) {

    @Operation(summary = "Listar los favoritos de un usuario")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Lista de favoritos del usuario")
    ])
    @GetMapping
    fun listar(@RequestParam usuarioId: Int): ResponseEntity<List<FavoritoDTO>> =
        ResponseEntity.ok(service.listar(usuarioId).map { FavoritoDTO.fromEntity(it) })

    @Operation(summary = "Agregar un favorito", description = "Debe incluir cuidador_id o mascota_id ademas de usuario_id.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Favorito agregado"),
        ApiResponse(responseCode = "400", description = "usuario_id, o cuidador_id/mascota_id faltantes")
    ])
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
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "Favorito eliminado"),
        ApiResponse(responseCode = "404", description = "Favorito no encontrado")
    ])
    @DeleteMapping("/{id}")
    fun eliminar(@PathVariable id: Int): ResponseEntity<*> {
        if (!service.existe(id)) {
            return ResponseEntity.status(404).body(mapOf("error" to "Favorito no encontrado"))
        }
        service.eliminar(id)
        return ResponseEntity.noContent().build<Any>()
    }
}
