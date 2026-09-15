package com.petcare.controller

/*
 * Comentario de modulo PetCare:
 * Controlador REST. Recibe peticiones HTTP, valida el flujo basico y delega la logica al servicio.
 */

import com.petcare.util.EticaUtil
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/reglas")
@Tag(name = "Reglas", description = "Reglas de negocio y ética de la plataforma")
class ReglasController {

    @Operation(summary = "Obtener el texto de la regla ética sobre venta de animales")
    @ApiResponses(value = [ApiResponse(responseCode = "200", description = "Texto de la regla ética")])
    @GetMapping("/etica")
    fun etica(): ResponseEntity<Map<String, String>> =
        ResponseEntity.ok(mapOf("texto" to EticaUtil.TEXTO_REGLA_ETICA.trimIndent()))
}
