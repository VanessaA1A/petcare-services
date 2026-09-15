package com.petcare.controller

/*
 * Comentario de modulo PetCare:
 * Controlador REST. Recibe peticiones HTTP, valida el flujo basico y delega la logica al servicio.
 */

import com.petcare.service.DashboardService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/dashboard")
@Tag(name = "Admin", description = "Métricas del panel de administración (Bloque 7)")
class AdminDashboardController(private val dashboardService: DashboardService) {

    @Operation(summary = "Estadísticas generales de la plataforma")
    @ApiResponses(value = [ApiResponse(responseCode = "200", description = "Estadísticas generales")])
    @GetMapping("/stats")
    fun stats() = ResponseEntity.ok(dashboardService.stats())

    @Operation(summary = "Actividad diaria de los últimos N días", description = "Cantidad de solicitudes publicadas por día.")
    @ApiResponses(value = [ApiResponse(responseCode = "200", description = "Serie de actividad diaria")])
    @GetMapping("/activity")
    fun activity(@RequestParam(required = false, defaultValue = "7") dias: Int) =
        ResponseEntity.ok(dashboardService.actividad(dias.coerceIn(1, 90)))
}
