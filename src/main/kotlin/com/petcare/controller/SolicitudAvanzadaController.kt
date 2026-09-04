package com.petcare.controller

/*
 * Comentario de modulo PetCare:
 * Controlador REST. Recibe peticiones HTTP, valida el flujo basico y delega la logica al servicio.
 */

import com.petcare.dto.ServiceRequestDTO
import com.petcare.model.ServiceRequest
import com.petcare.service.MobileServiceRequestService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/solicitudes")
@Tag(name = "Solicitudes Avanzadas", description = "Edicion, extension, reasignacion, historial y busqueda de solicitudes de servicio")
class SolicitudAvanzadaController(private val service: MobileServiceRequestService) {

    @Operation(summary = "Editar una solicitud de servicio", description = "Solo se permite mientras la solicitud esta en estado PENDING.")
    @PutMapping("/{id}")
    fun actualizar(@PathVariable id: Int, @RequestBody body: Map<String, Any?>): ResponseEntity<*> {
        return try {
            val updated = service.updateRequest(id, body.toPartialServiceRequest())
                ?: return ResponseEntity.status(404).body(mapOf("error" to "Solicitud no encontrada"))
            ResponseEntity.ok(ServiceRequestDTO.fromEntity(updated))
        } catch (ex: IllegalStateException) {
            ResponseEntity.badRequest().body(mapOf("error" to ex.message))
        }
    }

    @Operation(summary = "Extender el plazo de una solicitud", description = "Extiende la fecha de expiracion 24 horas. Solo aplica a solicitudes en estado PENDING.")
    @PostMapping("/{id}/extender")
    fun extender(@PathVariable id: Int): ResponseEntity<*> {
        if (service.findRequest(id).isEmpty) {
            return ResponseEntity.status(404).body(mapOf("error" to "Solicitud no encontrada"))
        }
        val updated = service.extendRequest(id)
            ?: return ResponseEntity.badRequest().body(mapOf("error" to "Solo se pueden extender solicitudes en estado PENDING"))
        return ResponseEntity.ok(ServiceRequestDTO.fromEntity(updated))
    }

    @Operation(summary = "Reasignar una solicitud", description = "Vuelve a poner en PENDING una solicitud CANCELLED o ACCEPTED y cancela las postulaciones aceptadas.")
    @PostMapping("/{id}/reasignar")
    fun reasignar(@PathVariable id: Int): ResponseEntity<*> {
        if (service.findRequest(id).isEmpty) {
            return ResponseEntity.status(404).body(mapOf("error" to "Solicitud no encontrada"))
        }
        val updated = service.reassignRequest(id)
            ?: return ResponseEntity.badRequest().body(mapOf("error" to "Solo se pueden reasignar solicitudes en estado CANCELLED o ACCEPTED"))
        return ResponseEntity.ok(ServiceRequestDTO.fromEntity(updated))
    }

    @Operation(summary = "Historial de solicitudes de un usuario", description = "Solicitudes en estado COMPLETED o CANCELLED. role puede ser OWNER o CAREGIVER.")
    @GetMapping("/historial")
    fun historial(
        @RequestParam usuarioId: Int,
        @RequestParam(required = false, defaultValue = "OWNER") role: String
    ): ResponseEntity<List<ServiceRequestDTO>> =
        ResponseEntity.ok(service.getHistory(usuarioId, role).map { ServiceRequestDTO.fromEntity(it) })

    @Operation(summary = "Buscar solicitudes de servicio", description = "Filtra por texto libre (q), tipo de servicio y estado. Sin status, solo busca en PENDING.")
    @GetMapping("/buscar")
    fun buscar(
        @RequestParam(required = false) q: String?,
        @RequestParam(required = false) serviceTypeId: Int?,
        @RequestParam(required = false) status: String?
    ): ResponseEntity<List<ServiceRequestDTO>> =
        ResponseEntity.ok(service.searchRequests(q, serviceTypeId, status).map { ServiceRequestDTO.fromEntity(it) })
}

private fun Map<String, Any?>.toPartialServiceRequest(): ServiceRequest {
    val request = ServiceRequest()
    request.title = stringValue("title")
    request.description = stringValue("description")
    request.requestedDate = stringValue("requested_date", "requestedDate")
    request.startTime = stringValue("start_time", "startTime")
    request.endTime = stringValue("end_time", "endTime")
    request.petId = nullableIntValue("pet_id", "petId")
    val petIds = intListValue("pet_ids", "petIds")
    if (petIds.isNotEmpty()) request.petIds = petIds.joinToString(",")
    request.serviceTypeId = nullableIntValue("service_type_id", "serviceTypeId")
    request.latitude = doubleValue("latitude")
    request.longitude = doubleValue("longitude")
    return request
}

private fun Map<String, Any?>.value(vararg keys: String): Any? =
    keys.firstNotNullOfOrNull { this[it] }

private fun Map<String, Any?>.stringValue(vararg keys: String): String? =
    value(*keys)?.toString()?.takeIf { it.isNotBlank() }

private fun Map<String, Any?>.nullableIntValue(vararg keys: String): Int? =
    when (val raw = value(*keys)) {
        is Number -> raw.toInt()
        is String -> raw.toIntOrNull()
        else -> null
    }

private fun Map<String, Any?>.doubleValue(vararg keys: String): Double? =
    when (val raw = value(*keys)) {
        is Number -> raw.toDouble()
        is String -> raw.toDoubleOrNull()
        else -> null
    }

private fun Map<String, Any?>.intListValue(vararg keys: String): List<Int> {
    val raw = value(*keys) ?: return emptyList()

    return when (raw) {
        is Collection<*> -> raw.mapNotNull {
            when (it) {
                is Number -> it.toInt()
                is String -> it.toIntOrNull()
                else -> null
            }
        }

        is String -> raw.split(',').mapNotNull { it.trim().toIntOrNull() }
        else -> emptyList()
    }
}
