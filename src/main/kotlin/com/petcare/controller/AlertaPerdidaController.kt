package com.petcare.controller

/*
 * Comentario de modulo PetCare:
 * Controlador REST. Recibe peticiones HTTP, valida el flujo basico y delega la logica al servicio.
 */

import com.petcare.dto.AlertaPerdidaCercanaDTO
import com.petcare.dto.AlertaPerdidaDTO
import com.petcare.dto.AvistamientoDTO
import com.petcare.exception.StorageException
import com.petcare.model.Avistamiento
import com.petcare.service.AlertaPerdidaService
import com.petcare.service.FileStorageService
import com.petcare.service.MascotaNoEncontradaException
import com.petcare.service.NoAutorizadoException
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
@RequestMapping("/api/alertas-perdida")
@Tag(name = "Alertas de mascota perdida", description = "Bloque 12: reporte de mascota perdida, notificacion a usuarios cercanos y avistamientos")
class AlertaPerdidaController(
    private val service: AlertaPerdidaService,
    private val fileStorageService: FileStorageService
) {

    @Operation(
        summary = "Reportar una mascota como perdida",
        description = "Solo el dueno de la mascota. Notifica por push a usuarios cercanos en 1km (urgente), 5km (ampliado) y 10km (zona)."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Alerta activada"),
        ApiResponse(responseCode = "400", description = "pets_id, usuario_id, latitud o longitud invalidos"),
        ApiResponse(responseCode = "403", description = "El usuario no es el dueno de la mascota")
    ])
    @PostMapping
    fun activar(@RequestBody request: AlertaPerdidaDTO): ResponseEntity<*> {
        if (request.petsId <= 0 || request.usuarioId <= 0) {
            return ResponseEntity.badRequest().body(mapOf("error" to "pets_id y usuario_id son requeridos"))
        }
        return try {
            val saved = service.activar(request.toEntity())
            ResponseEntity.status(201).body(AlertaPerdidaDTO.fromEntity(saved))
        } catch (ex: NoAutorizadoException) {
            ResponseEntity.status(403).body(mapOf("error" to ex.message))
        }
    }

    @Operation(
        summary = "Reportar un avistamiento",
        description = "Cualquier usuario puede reportar haber visto a la mascota. Requiere una foto (multipart/form-data)."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Avistamiento registrado"),
        ApiResponse(responseCode = "400", description = "usuario_id o foto faltantes/invalidos"),
        ApiResponse(responseCode = "404", description = "Alerta no encontrada")
    ])
    @PostMapping("/{id}/avistamiento", consumes = ["multipart/form-data"])
    fun reportarAvistamiento(
        @PathVariable id: Int,
        @RequestParam("usuario_id") usuarioId: Int,
        @RequestParam(required = false) comentario: String?,
        @RequestParam(required = false) latitud: Double?,
        @RequestParam(required = false) longitud: Double?,
        @RequestParam(required = false) foto: MultipartFile?
    ): ResponseEntity<*> {
        if (usuarioId <= 0) {
            return ResponseEntity.badRequest().body(mapOf("error" to "usuario_id es requerido"))
        }
        if (foto == null || foto.isEmpty) {
            return ResponseEntity.badRequest().body(mapOf("error" to "foto es requerida"))
        }
        return try {
            val imagenUrl = "/api/alertas-perdida/avistamiento/foto/${fileStorageService.storeAvistamientoImage(id, foto)}"
            val entrada = Avistamiento(
                alertaId = id,
                usuarioId = usuarioId,
                latitud = latitud,
                longitud = longitud,
                comentario = comentario,
                imagenUrl = imagenUrl
            )
            val saved = service.reportarAvistamiento(entrada)
            ResponseEntity.status(201).body(AvistamientoDTO.fromEntity(saved))
        } catch (ex: MascotaNoEncontradaException) {
            ResponseEntity.status(404).body(mapOf("error" to ex.message))
        } catch (ex: StorageException) {
            ResponseEntity.badRequest().body(mapOf("error" to ex.message))
        }
    }

    @Operation(summary = "Servir la foto de un avistamiento por nombre de archivo")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Imagen servida"),
        ApiResponse(responseCode = "404", description = "Imagen no encontrada")
    ])
    @GetMapping("/avistamiento/foto/{filename}")
    fun servirFotoAvistamiento(@PathVariable filename: String): ResponseEntity<*> {
        return try {
            val path: Path = fileStorageService.loadAvistamientoImage(filename)
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

    @Operation(summary = "Listar los avistamientos de una alerta")
    @ApiResponses(value = [ApiResponse(responseCode = "200", description = "Avistamientos de la alerta")])
    @GetMapping("/{id}/avistamientos")
    fun avistamientos(@PathVariable id: Int): ResponseEntity<List<AvistamientoDTO>> =
        ResponseEntity.ok(service.avistamientos(id).map { AvistamientoDTO.fromEntity(it) })

    @Operation(summary = "Marcar una mascota como encontrada", description = "Solo el dueno que reporto la alerta puede cerrarla.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Alerta cerrada como encontrada"),
        ApiResponse(responseCode = "403", description = "El usuario no reporto esta alerta"),
        ApiResponse(responseCode = "404", description = "Alerta no encontrada")
    ])
    @PutMapping("/{id}/encontrada")
    fun marcarEncontrada(@PathVariable id: Int, @RequestParam("usuario_id") usuarioId: Int): ResponseEntity<*> {
        return try {
            val actualizada = service.marcarEncontrada(usuarioId, id)
                ?: return ResponseEntity.status(404).body(mapOf("error" to "Alerta no encontrada"))
            ResponseEntity.ok(AlertaPerdidaDTO.fromEntity(actualizada))
        } catch (ex: NoAutorizadoException) {
            ResponseEntity.status(403).body(mapOf("error" to ex.message))
        }
    }

    @Operation(summary = "Buscar alertas de mascota perdida cercanas", description = "Radio en kilometros (por defecto 10). Solo alertas ACTIVA.")
    @ApiResponses(value = [ApiResponse(responseCode = "200", description = "Alertas cercanas")])
    @GetMapping("/cercanas")
    fun cercanas(
        @RequestParam lat: Double,
        @RequestParam lng: Double,
        @RequestParam(required = false, defaultValue = "10") radio: Double
    ): ResponseEntity<List<AlertaPerdidaCercanaDTO>> {
        val resultado = service.cercanas(lat, lng, radio)
            .map { (alerta, distancia) -> AlertaPerdidaCercanaDTO(AlertaPerdidaDTO.fromEntity(alerta), distancia) }
        return ResponseEntity.ok(resultado)
    }
}
