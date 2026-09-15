package com.petcare.controller

/*
 * Comentario de modulo PetCare:
 * Controlador REST. Recibe peticiones HTTP, valida el flujo basico y delega la logica al servicio.
 */

import com.petcare.dto.EvidenciaServicioDTO
import com.petcare.dto.ServiceRequestDTO
import com.petcare.dto.ValoracionTiempoRealDTO
import com.petcare.exception.StorageException
import com.petcare.model.EvidenciaServicio
import com.petcare.model.ServiceRequest
import com.petcare.service.EvidenciaServicioService
import com.petcare.service.FileStorageService
import com.petcare.service.MobileServiceRequestService
import com.petcare.service.SolicitudNoEnCursoException
import com.petcare.service.ValoracionTiempoRealService
import com.petcare.websocket.LiveLocationRegistry
import com.petcare.websocket.WsEvent
import com.petcare.websocket.WsEventService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.net.MalformedURLException
import java.nio.file.Path

@RestController
@RequestMapping("/api/solicitudes")
@Tag(name = "Solicitudes Avanzadas", description = "Edicion, extension, reasignacion, historial y busqueda de solicitudes de servicio")
class SolicitudAvanzadaController(
    private val service: MobileServiceRequestService,
    private val wsEventService: WsEventService,
    private val liveLocationRegistry: LiveLocationRegistry,
    private val valoracionTiempoRealService: ValoracionTiempoRealService,
    private val evidenciaService: EvidenciaServicioService,
    private val fileStorageService: FileStorageService
) {

    @Operation(summary = "Editar una solicitud de servicio", description = "Solo se permite mientras la solicitud esta en estado PENDING.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Solicitud actualizada"),
        ApiResponse(responseCode = "400", description = "La solicitud no esta en estado PENDING"),
        ApiResponse(responseCode = "404", description = "Solicitud no encontrada")
    ])
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
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Plazo extendido"),
        ApiResponse(responseCode = "400", description = "La solicitud no esta en estado PENDING"),
        ApiResponse(responseCode = "404", description = "Solicitud no encontrada")
    ])
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
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Solicitud reasignada"),
        ApiResponse(responseCode = "400", description = "La solicitud no esta en estado CANCELLED ni ACCEPTED"),
        ApiResponse(responseCode = "404", description = "Solicitud no encontrada")
    ])
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
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Historial de solicitudes")
    ])
    @GetMapping("/historial")
    fun historial(
        @RequestParam usuarioId: Int,
        @RequestParam(required = false, defaultValue = "OWNER") role: String
    ): ResponseEntity<List<ServiceRequestDTO>> =
        ResponseEntity.ok(service.getHistory(usuarioId, role).map { ServiceRequestDTO.fromEntity(it) })

    @Operation(summary = "Buscar solicitudes de servicio", description = "Filtra por texto libre (q), tipo de servicio y estado. Sin status, solo busca en PENDING.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Solicitudes encontradas")
    ])
    @GetMapping("/buscar")
    fun buscar(
        @RequestParam(required = false) q: String?,
        @RequestParam(required = false) serviceTypeId: Int?,
        @RequestParam(required = false) status: String?
    ): ResponseEntity<List<ServiceRequestDTO>> =
        ResponseEntity.ok(service.searchRequests(q, serviceTypeId, status).map { ServiceRequestDTO.fromEntity(it) })

    @Operation(summary = "Actualizar la ubicacion en vivo de un servicio en curso", description = "El cuidador envia su posicion cada pocos segundos mientras el servicio esta EN_PROGRESO (taxi, paseo). Se retransmite al propietario por WebSocket.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Ubicacion registrada"),
        ApiResponse(responseCode = "400", description = "latitud/longitud faltantes o invalidas"),
        ApiResponse(responseCode = "404", description = "Solicitud no encontrada")
    ])
    @PostMapping("/{id}/ubicacion")
    fun actualizarUbicacion(@PathVariable id: Int, @RequestBody body: Map<String, Any?>): ResponseEntity<*> {
        val latitud = (body["latitud"] as? Number)?.toDouble()
        val longitud = (body["longitud"] as? Number)?.toDouble()
        if (latitud == null || longitud == null) {
            return ResponseEntity.badRequest().body(mapOf("error" to "latitud y longitud son requeridas y deben ser numericas"))
        }

        val request = service.findRequest(id).orElse(null)
            ?: return ResponseEntity.status(404).body(mapOf("error" to "Solicitud no encontrada"))

        liveLocationRegistry.update(id, latitud, longitud)

        val ownerId = request.ownerId
        if (ownerId != null) {
            wsEventService.sendToUser(
                ownerId,
                WsEvent(
                    type = "LOCATION_UPDATE",
                    recipientUserId = ownerId,
                    title = "Ubicacion actualizada",
                    message = "El cuidador actualizo su ubicacion en el servicio #$id",
                    serviceRequestId = id,
                    latitude = latitud,
                    longitude = longitud
                )
            )
        }

        return ResponseEntity.ok(mapOf("latitud" to latitud, "longitud" to longitud))
    }

    @Operation(summary = "Obtener la ultima ubicacion conocida de un servicio en curso")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Ultima ubicacion conocida"),
        ApiResponse(responseCode = "404", description = "No hay ubicacion registrada todavia para esta solicitud")
    ])
    @GetMapping("/{id}/ubicacion-actual")
    fun obtenerUbicacionActual(@PathVariable id: Int): ResponseEntity<*> {
        val location = liveLocationRegistry.get(id)
            ?: return ResponseEntity.status(404).body(mapOf("error" to "No hay ubicacion registrada todavia para esta solicitud"))

        return ResponseEntity.ok(
            mapOf(
                "latitud" to location.latitude,
                "longitud" to location.longitude,
                "actualizadoEn" to location.updatedAt
            )
        )
    }

    @Operation(summary = "Enviar una reaccion en tiempo real", description = "Reaccion rapida (corazon/estrella/pulgar) mientras el servicio esta en curso (estado ACCEPTED). No reemplaza la calificacion final.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Reaccion registrada"),
        ApiResponse(responseCode = "400", description = "usuario_id o tipo_reaccion invalidos, o la solicitud no esta en curso")
    ])
    @PostMapping("/{id}/valorar-durante")
    fun valorarDurante(@PathVariable id: Int, @RequestBody body: Map<String, Any?>): ResponseEntity<*> {
        val usuarioId = (body["usuario_id"] as? Number)?.toInt() ?: (body["usuarioId"] as? Number)?.toInt()
        val tipoReaccion = (body["tipo_reaccion"] as? String) ?: (body["tipoReaccion"] as? String)

        if (usuarioId == null || usuarioId <= 0 || tipoReaccion !in ValoracionTiempoRealDTO.TIPOS_VALIDOS) {
            return ResponseEntity.badRequest().body(
                mapOf("error" to "usuario_id es requerido y tipo_reaccion debe ser uno de ${ValoracionTiempoRealDTO.TIPOS_VALIDOS}")
            )
        }

        return try {
            val dto = ValoracionTiempoRealDTO(serviceRequestId = id, usuarioId = usuarioId, tipoReaccion = tipoReaccion!!)
            val saved = valoracionTiempoRealService.valorar(dto.toEntity())
            ResponseEntity.status(201).body(ValoracionTiempoRealDTO.fromEntity(saved))
        } catch (ex: SolicitudNoEnCursoException) {
            ResponseEntity.badRequest().body(mapOf("error" to ex.message))
        }
    }

    @Operation(
        summary = "Subir evidencia fotografica (antes/despues) de un servicio",
        description = "Foto obligatoria en la app al iniciar (ANTES) y terminar (DESPUES) un servicio. " +
            "Este endpoint no bloquea ningun cambio de estado: si el dispositivo no tiene internet a tiempo, " +
            "la app permite igual el cambio de estado y sube la foto despues, al reconectar."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Evidencia guardada"),
        ApiResponse(responseCode = "400", description = "tipo invalido o archivo vacio/tipo no permitido")
    ])
    @PostMapping("/{id}/evidencia", consumes = ["multipart/form-data"])
    fun subirEvidencia(
        @PathVariable id: Int,
        @RequestParam tipo: String,
        @RequestParam(required = false) nota: String?,
        @RequestParam(required = false) latitud: Double?,
        @RequestParam(required = false) longitud: Double?,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<*> {
        if (tipo !in EvidenciaServicioDTO.TIPOS_VALIDOS) {
            return ResponseEntity.badRequest().body(mapOf("error" to "tipo debe ser uno de ${EvidenciaServicioDTO.TIPOS_VALIDOS}"))
        }
        return try {
            val filename = fileStorageService.storeEvidenciaImage(id, tipo, file)
            val entity = EvidenciaServicio(
                solicitudId = id,
                tipo = tipo,
                imagenUrl = "/api/solicitudes/evidencia/$filename",
                nota = nota,
                latitud = latitud,
                longitud = longitud
            )
            val saved = evidenciaService.guardar(entity)
            ResponseEntity.status(201).body(EvidenciaServicioDTO.fromEntity(saved))
        } catch (ex: StorageException) {
            ResponseEntity.badRequest().body(mapOf("error" to ex.message))
        }
    }

    @Operation(summary = "Listar la evidencia fotografica (antes/despues) de un servicio")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Evidencia del servicio")
    ])
    @GetMapping("/{id}/evidencias")
    fun listarEvidencias(@PathVariable id: Int): ResponseEntity<List<EvidenciaServicioDTO>> =
        ResponseEntity.ok(evidenciaService.listar(id).map { EvidenciaServicioDTO.fromEntity(it) })

    @Operation(summary = "Servir una imagen de evidencia por nombre de archivo")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Imagen servida"),
        ApiResponse(responseCode = "404", description = "Imagen no encontrada")
    ])
    @GetMapping("/evidencia/{filename}")
    fun servirEvidencia(@PathVariable filename: String): ResponseEntity<*> {
        return try {
            val path: Path = fileStorageService.loadEvidenciaImage(filename)
            val resource: Resource = UrlResource(path.toUri())
            val contentType = when (path.toString().substringAfterLast('.', "jpg").lowercase()) {
                "png" -> MediaType.IMAGE_PNG
                "gif" -> MediaType.IMAGE_GIF
                "webp" -> MediaType.valueOf("image/webp")
                else -> MediaType.IMAGE_JPEG
            }
            ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"${path.fileName}\"")
                .body(resource)
        } catch (ex: StorageException) {
            ResponseEntity.status(404).body(mapOf("error" to ex.message))
        } catch (ex: MalformedURLException) {
            ResponseEntity.status(404).body(mapOf("error" to "Imagen no encontrada"))
        }
    }
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
