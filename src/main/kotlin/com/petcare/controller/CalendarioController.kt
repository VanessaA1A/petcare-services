package com.petcare.controller

/*
 * Comentario de modulo PetCare:
 * Controlador REST. Recibe peticiones HTTP, valida el flujo basico y delega la logica al servicio.
 */

import com.petcare.dto.CalendarioResponse
import com.petcare.service.CalendarioService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/calendario")
@Tag(name = "Calendario", description = "Vista de calendario con los servicios programados de un usuario (Bloque 9)")
class CalendarioController(private val service: CalendarioService) {

    @Operation(
        summary = "Obtener el calendario de un usuario para un mes",
        description = "Servicios programados (como propietario o como cuidador) en el mes/anio indicado. " +
            "usuario_id es requerido (no forma parte del prompt original, pero es necesario para no exponer " +
            "el calendario de todos los usuarios publicamente)."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Calendario del usuario"),
        ApiResponse(responseCode = "400", description = "usuario_id, mes o anio invalidos")
    ])
    @GetMapping
    fun obtenerCalendario(
        @RequestParam(name = "usuario_id") usuarioId: Int,
        @RequestParam mes: Int,
        @RequestParam anio: Int
    ): ResponseEntity<*> {
        if (usuarioId <= 0 || mes !in 1..12 || anio < 2000) {
            return ResponseEntity.badRequest().body(mapOf("error" to "usuario_id, mes (1-12) y anio son requeridos y deben ser validos"))
        }
        val calendario: CalendarioResponse = service.obtenerCalendario(usuarioId, mes, anio)
        return ResponseEntity.ok(calendario)
    }
}
