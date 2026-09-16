package com.petcare.controller

/*
 * Comentario de modulo PetCare:
 * Controlador REST. Recibe peticiones HTTP, valida el flujo basico y delega la logica al servicio.
 */

import com.petcare.model.DisponibilidadCuidador
import com.petcare.service.DisponibilidadCuidadorService
import com.petcare.service.NoAutorizadoException
import com.petcare.service.SolapamientoDisponibilidadException
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalTime
import java.time.format.DateTimeParseException

@RestController
@Tag(name = "Disponibilidad del cuidador", description = "Bloque 9: horario semanal recurrente que el cuidador publica, expandido en fechas concretas por GET /api/calendario")
class DisponibilidadCuidadorController(
    private val service: DisponibilidadCuidadorService
) {
    @Operation(summary = "Publicar un bloque de disponibilidad semanal", description = "Solo un usuario con rol cuidador puede llamar este endpoint. dia_semana: 0=lunes ... 6=domingo. hora_inicio/hora_fin en formato HH:mm.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Bloque de disponibilidad creado"),
        ApiResponse(responseCode = "400", description = "Datos invalidos o el horario se solapa con uno existente"),
        ApiResponse(responseCode = "403", description = "El usuario no es cuidador")
    ])
    @PostMapping("/api/cuidadores/disponibilidad")
    fun crear(@RequestBody body: Map<String, Any?>): ResponseEntity<*> {
        val usuarioId = (body["usuario_id"] as? Number)?.toInt()
            ?: return ResponseEntity.badRequest().body(mapOf("error" to "usuario_id es requerido"))
        val diaSemana = (body["dia_semana"] as? Number)?.toInt()
        if (diaSemana == null || diaSemana !in 0..6) {
            return ResponseEntity.badRequest().body(mapOf("error" to "dia_semana debe estar entre 0 (lunes) y 6 (domingo)"))
        }
        val horaInicio = runCatching { LocalTime.parse(body["hora_inicio"] as? String) }.getOrNull()
        val horaFin = runCatching { LocalTime.parse(body["hora_fin"] as? String) }.getOrNull()
        if (horaInicio == null || horaFin == null) {
            return ResponseEntity.badRequest().body(mapOf("error" to "hora_inicio y hora_fin son requeridos en formato HH:mm"))
        }

        return try {
            val creado = service.crear(usuarioId, diaSemana, horaInicio, horaFin)
            ResponseEntity.status(201).body(creado)
        } catch (ex: NoAutorizadoException) {
            ResponseEntity.status(403).body(mapOf("error" to ex.message))
        } catch (ex: SolapamientoDisponibilidadException) {
            ResponseEntity.badRequest().body(mapOf("error" to ex.message))
        } catch (ex: DateTimeParseException) {
            ResponseEntity.badRequest().body(mapOf("error" to "hora_inicio/hora_fin invalidas, formato esperado HH:mm"))
        }
    }

    @Operation(summary = "Listar la disponibilidad semanal publicada por un cuidador")
    @ApiResponses(value = [ApiResponse(responseCode = "200", description = "Bloques de disponibilidad activos, ordenados por dia y hora")])
    @GetMapping("/api/cuidadores/{id}/disponibilidad")
    fun listar(@PathVariable id: Int): ResponseEntity<List<DisponibilidadCuidador>> =
        ResponseEntity.ok(service.listarPorCuidador(id))

    @Operation(summary = "Eliminar un bloque de disponibilidad", description = "Solo el cuidador dueño del horario puede eliminarlo.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "Bloque eliminado"),
        ApiResponse(responseCode = "403", description = "El usuario no es dueño de este horario"),
        ApiResponse(responseCode = "404", description = "Bloque no encontrado")
    ])
    @DeleteMapping("/api/cuidadores/disponibilidad/{id}")
    fun eliminar(
        @PathVariable id: Int,
        @Parameter(description = "id del usuario que hace la peticion") @RequestParam("usuario_id") usuarioId: Int
    ): ResponseEntity<*> {
        return try {
            if (!service.eliminar(id, usuarioId)) {
                return ResponseEntity.status(404).body(mapOf("error" to "Bloque de disponibilidad no encontrado"))
            }
            ResponseEntity.noContent().build<Any>()
        } catch (ex: NoAutorizadoException) {
            ResponseEntity.status(403).body(mapOf("error" to ex.message))
        }
    }
}
