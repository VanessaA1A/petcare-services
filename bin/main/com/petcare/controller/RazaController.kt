package com.petcare.controller

/*
 * Comentario de modulo PetCare:
 * Controlador REST. Recibe peticiones HTTP, valida el flujo basico y delega la logica al servicio.
 */

import com.petcare.service.DogBreedService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/razas")
@Tag(name = "Razas", description = "Autocompletado de razas de perros (Dog CEO API, cacheado 24h)")
class RazaController(private val dogBreedService: DogBreedService) {

    @Operation(summary = "Buscar razas de perros", description = "Filtra por texto (case-insensitive). Siempre incluye Mixto, Desconocido, Criollo y No especificado.")
    @GetMapping
    fun search(@RequestParam(required = false) q: String?): ResponseEntity<Map<String, Any>> =
        ResponseEntity.ok(mapOf("razas" to dogBreedService.search(q)))
}
