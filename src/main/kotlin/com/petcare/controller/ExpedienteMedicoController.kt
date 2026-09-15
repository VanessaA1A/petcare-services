package com.petcare.controller

/*
 * Comentario de modulo PetCare:
 * Controlador REST. Recibe peticiones HTTP, valida el flujo basico y delega la logica al servicio.
 */

import com.petcare.dto.ExpedienteMedicoDTO
import com.petcare.exception.StorageException
import com.petcare.model.ExpedienteMedico
import com.petcare.service.ExpedienteMedicoService
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
import java.time.LocalDate

@RestController
@RequestMapping("/api/pets")
@Tag(name = "Expediente medico", description = "Historial medico de una mascota (Bloque 11): vacunas, desparasitacion, alergias, medicamentos, cirugias, peso y notas")
class ExpedienteMedicoController(
    private val service: ExpedienteMedicoService,
    private val fileStorageService: FileStorageService
) {
    @Operation(summary = "Listar el expediente medico de una mascota")
    @ApiResponses(value = [ApiResponse(responseCode = "200", description = "Entradas del expediente, mas recientes primero")])
    @GetMapping("/{id}/expediente")
    fun listar(@PathVariable id: Int): ResponseEntity<List<ExpedienteMedicoDTO>> =
        ResponseEntity.ok(service.listar(id).map { ExpedienteMedicoDTO.fromEntity(it) })

    @Operation(
        summary = "Agregar una entrada al expediente medico",
        description = "Solo el dueno de la mascota puede agregar entradas. Permite adjuntar opcionalmente una foto del carnet de vacunas."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Entrada creada"),
        ApiResponse(responseCode = "400", description = "Campos requeridos faltantes o invalidos"),
        ApiResponse(responseCode = "403", description = "El usuario no es el dueno de la mascota"),
        ApiResponse(responseCode = "404", description = "Mascota no encontrada")
    ])
    @PostMapping("/{id}/expediente", consumes = ["multipart/form-data"])
    fun crear(
        @PathVariable id: Int,
        @RequestParam("usuario_id") usuarioId: Int,
        @RequestParam tipo: String,
        @RequestParam titulo: String,
        @RequestParam(required = false) descripcion: String?,
        @RequestParam fecha: String,
        @RequestParam(name = "fecha_proxima", required = false) fechaProxima: String?,
        @RequestParam(name = "veterinario_nombre", required = false) veterinarioNombre: String?,
        @RequestParam(name = "veterinario_telefono", required = false) veterinarioTelefono: String?,
        @RequestParam(required = false) file: MultipartFile?
    ): ResponseEntity<*> {
        if (tipo !in ExpedienteMedicoDTO.TIPOS_VALIDOS || titulo.isBlank()) {
            return ResponseEntity.badRequest().body(
                mapOf("error" to "titulo es requerido y tipo debe ser uno de ${ExpedienteMedicoDTO.TIPOS_VALIDOS}")
            )
        }
        val fechaParseada = runCatching { LocalDate.parse(fecha) }.getOrNull()
            ?: return ResponseEntity.badRequest().body(mapOf("error" to "fecha invalida, formato esperado YYYY-MM-DD"))

        return try {
            val entrada = ExpedienteMedico(
                petsId = id,
                tipo = tipo,
                titulo = titulo,
                descripcion = descripcion,
                fecha = fechaParseada,
                fechaProxima = fechaProxima?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
                veterinarioNombre = veterinarioNombre,
                veterinarioTelefono = veterinarioTelefono,
                imagenCarnetUrl = file?.takeUnless { it.isEmpty }?.let { f ->
                    "/api/pets/expediente/carnet/${fileStorageService.storeCarnetImage(id, f)}"
                }
            )
            val saved = service.crear(usuarioId, entrada)
            ResponseEntity.status(201).body(ExpedienteMedicoDTO.fromEntity(saved))
        } catch (ex: MascotaNoEncontradaException) {
            ResponseEntity.status(404).body(mapOf("error" to ex.message))
        } catch (ex: NoAutorizadoException) {
            ResponseEntity.status(403).body(mapOf("error" to ex.message))
        } catch (ex: StorageException) {
            ResponseEntity.badRequest().body(mapOf("error" to ex.message))
        }
    }

    @Operation(summary = "Actualizar una entrada del expediente medico", description = "Solo el dueno de la mascota puede editarla.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Entrada actualizada"),
        ApiResponse(responseCode = "403", description = "El usuario no es el dueno de la mascota"),
        ApiResponse(responseCode = "404", description = "Entrada no encontrada")
    ])
    @PutMapping("/expediente/{entradaId}")
    fun actualizar(
        @PathVariable entradaId: Int,
        @RequestParam("usuario_id") usuarioId: Int,
        @RequestBody body: ExpedienteMedicoDTO
    ): ResponseEntity<*> {
        return try {
            val actualizada = service.actualizar(usuarioId, entradaId, body.toEntity())
                ?: return ResponseEntity.status(404).body(mapOf("error" to "Entrada no encontrada"))
            ResponseEntity.ok(ExpedienteMedicoDTO.fromEntity(actualizada))
        } catch (ex: NoAutorizadoException) {
            ResponseEntity.status(403).body(mapOf("error" to ex.message))
        }
    }

    @Operation(summary = "Eliminar una entrada del expediente medico", description = "Solo el dueno de la mascota puede eliminarla.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "Entrada eliminada"),
        ApiResponse(responseCode = "403", description = "El usuario no es el dueno de la mascota"),
        ApiResponse(responseCode = "404", description = "Entrada no encontrada")
    ])
    @DeleteMapping("/expediente/{entradaId}")
    fun eliminar(@PathVariable entradaId: Int, @RequestParam("usuario_id") usuarioId: Int): ResponseEntity<*> {
        return try {
            if (!service.eliminar(usuarioId, entradaId)) {
                return ResponseEntity.status(404).body(mapOf("error" to "Entrada no encontrada"))
            }
            ResponseEntity.noContent().build<Any>()
        } catch (ex: NoAutorizadoException) {
            ResponseEntity.status(403).body(mapOf("error" to ex.message))
        }
    }

    @Operation(summary = "Servir la foto del carnet de vacunas por nombre de archivo")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Imagen servida"),
        ApiResponse(responseCode = "404", description = "Imagen no encontrada")
    ])
    @GetMapping("/expediente/carnet/{filename}")
    fun servirCarnet(@PathVariable filename: String): ResponseEntity<*> {
        return try {
            val path: Path = fileStorageService.loadCarnetImage(filename)
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
