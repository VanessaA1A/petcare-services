package com.petcare.controller

/*
 * Comentario de modulo PetCare:
 * Controlador REST. Recibe peticiones HTTP, valida el flujo basico y delega la logica al servicio.
 */

import com.petcare.dto.BusquedaGuardadaDTO
import com.petcare.service.BusquedaGuardadaService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/busquedas-guardadas")
@Tag(name = "Busquedas guardadas", description = "Filtros de busqueda guardados por un usuario para reutilizarlos")
class BusquedaGuardadaController(private val service: BusquedaGuardadaService) {

    @Operation(summary = "Listar las busquedas guardadas de un usuario")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Lista de busquedas guardadas del usuario")
    ])
    @GetMapping
    fun listar(@RequestParam usuarioId: Int): ResponseEntity<List<BusquedaGuardadaDTO>> =
        ResponseEntity.ok(service.listar(usuarioId).map { BusquedaGuardadaDTO.fromEntity(it) })

    @Operation(summary = "Guardar una busqueda", description = "filtros_json debe ser un JSON serializado como texto con los filtros aplicados.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Busqueda guardada"),
        ApiResponse(responseCode = "400", description = "usuario_id, nombre o filtros_json faltantes")
    ])
    @PostMapping
    fun guardar(@RequestBody request: BusquedaGuardadaDTO): ResponseEntity<*> {
        if (request.usuarioId <= 0 || request.nombre.isBlank() || request.filtrosJson.isBlank()) {
            return ResponseEntity.badRequest()
                .body(mapOf("error" to "usuario_id, nombre y filtros_json son requeridos"))
        }
        val saved = service.guardar(request.toEntity())
        return ResponseEntity.status(201).body(BusquedaGuardadaDTO.fromEntity(saved))
    }

    @Operation(summary = "Eliminar una busqueda guardada")
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "Busqueda guardada eliminada"),
        ApiResponse(responseCode = "404", description = "Busqueda no encontrada")
    ])
    @DeleteMapping("/{id}")
    fun eliminar(@PathVariable id: Int): ResponseEntity<*> {
        if (!service.existe(id)) {
            return ResponseEntity.status(404).body(mapOf("error" to "Busqueda no encontrada"))
        }
        service.eliminar(id)
        return ResponseEntity.noContent().build<Any>()
    }
}
