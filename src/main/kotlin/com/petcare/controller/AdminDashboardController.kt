package com.petcare.controller

/*
 * Comentario de modulo PetCare:
 * Controlador REST. Recibe peticiones HTTP, valida el flujo basico y delega la logica al servicio.
 */

import com.petcare.security.AdminGuard
import com.petcare.service.DashboardService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/dashboard")
@Tag(name = "Admin", description = "Métricas del panel de administración (Bloque 7) - requiere login de administrador")
class AdminDashboardController(
    private val dashboardService: DashboardService,
    private val adminGuard: AdminGuard
) {
    @Operation(summary = "Estadísticas generales de la plataforma")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Estadísticas generales"),
        ApiResponse(responseCode = "401", description = "No autenticado o la cuenta no es administrador")
    ])
    @GetMapping("/stats")
    fun stats(@RequestHeader(value = "Authorization", required = false) auth: String?): ResponseEntity<*> {
        adminGuard.adminFromBearer(auth) ?: return ResponseEntity.status(401).body(mapOf("error" to "No autenticado como administrador"))
        return ResponseEntity.ok(dashboardService.stats())
    }

    @Operation(summary = "Actividad diaria de los últimos N días", description = "Cantidad de solicitudes publicadas por día.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Serie de actividad diaria"),
        ApiResponse(responseCode = "401", description = "No autenticado o la cuenta no es administrador")
    ])
    @GetMapping("/activity")
    fun activity(
        @RequestHeader(value = "Authorization", required = false) auth: String?,
        @RequestParam(required = false, defaultValue = "7") dias: Int
    ): ResponseEntity<*> {
        adminGuard.adminFromBearer(auth) ?: return ResponseEntity.status(401).body(mapOf("error" to "No autenticado como administrador"))
        return ResponseEntity.ok(dashboardService.actividad(dias.coerceIn(1, 90)))
    }
}
