package com.petcare.controller

/*
 * Comentario de modulo PetCare:
 * Controlador REST. Recibe peticiones HTTP, valida el flujo basico y delega la logica al servicio.
 */

import com.petcare.dto.EmergenciaDTO
import com.petcare.service.EmergenciaService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/emergencias")
@Tag(name = "Emergencias", description = "Boton de emergencia durante un servicio en curso")
class EmergenciaController(private val service: EmergenciaService) {

    @Operation(summary = "Reportar una emergencia", description = "Guarda la emergencia y notifica por push al dueno, al cuidador asignado y a los administradores.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Emergencia registrada"),
        ApiResponse(responseCode = "400", description = "service_request_id, reported_by o tipo invalidos")
    ])
    @PostMapping
    fun reportar(@RequestBody request: EmergenciaDTO): ResponseEntity<*> {
        if (request.serviceRequestId <= 0 || request.reportedBy <= 0 || request.tipo !in EmergenciaDTO.TIPOS_VALIDOS) {
            return ResponseEntity.badRequest().body(
                mapOf("error" to "service_request_id, reported_by son requeridos y tipo debe ser uno de ${EmergenciaDTO.TIPOS_VALIDOS}")
            )
        }
        val saved = service.reportar(request.toEntity())
        return ResponseEntity.status(201).body(EmergenciaDTO.fromEntity(saved))
    }

    @Operation(summary = "Listar todas las emergencias reportadas", description = "Uso administrativo: historial completo, mas recientes primero.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Listado de emergencias")
    ])
    @GetMapping
    fun listar(): ResponseEntity<List<EmergenciaDTO>> =
        ResponseEntity.ok(service.listar().map { EmergenciaDTO.fromEntity(it) })
}
