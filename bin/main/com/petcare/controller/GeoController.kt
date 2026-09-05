package com.petcare.controller

/*
 * Comentario de modulo PetCare:
 * Controlador REST. Recibe peticiones HTTP, valida el flujo basico y delega la logica al servicio.
 */

import com.petcare.dto.NearbyCaregiverDTO
import com.petcare.dto.NearbyOfferedServiceDTO
import com.petcare.dto.NearbyServiceRequestDTO
import com.petcare.dto.OfferedServiceDTO
import com.petcare.dto.ServiceRequestDTO
import com.petcare.model.User
import com.petcare.repository.UserRepository
import com.petcare.service.GeocodeResult
import com.petcare.service.GeocodingService
import com.petcare.service.MobileServiceRequestService
import com.petcare.service.OfferedServiceService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/geo")
@Tag(name = "Geolocalizacion", description = "Geocodificacion (Nominatim, limitado a Nicaragua) y busqueda por cercania")
class GeoController(
    private val geocodingService: GeocodingService,
    private val requestService: MobileServiceRequestService,
    private val offeredServiceService: OfferedServiceService,
    private val userRepository: UserRepository
) {
    @Operation(summary = "Convertir una direccion de texto en coordenadas", description = "Usa Nominatim (OpenStreetMap), limitado a Nicaragua.")
    @GetMapping("/geocode")
    fun geocode(@RequestParam direccion: String): ResponseEntity<*> {
        val result: GeocodeResult = geocodingService.geocode(direccion)
            ?: return ResponseEntity.status(404).body(mapOf("error" to "No se encontro esa direccion"))
        return ResponseEntity.ok(result)
    }

    @Operation(summary = "Solicitudes de servicio abiertas cerca de una coordenada", description = "Radio en kilometros (por defecto 15).")
    @GetMapping("/solicitudes-cercanas")
    fun solicitudesCercanas(
        @RequestParam lat: Double,
        @RequestParam lng: Double,
        @RequestParam(required = false, defaultValue = "15") radio: Double
    ): ResponseEntity<List<NearbyServiceRequestDTO>> {
        val cercanas = requestService.available()
            .mapNotNull { request ->
                val rlat = request.latitude ?: return@mapNotNull null
                val rlng = request.longitude ?: return@mapNotNull null
                val distancia = geocodingService.calcularDistanciaKm(lat, lng, rlat, rlng)
                if (distancia > radio) null else NearbyServiceRequestDTO(ServiceRequestDTO.fromEntity(request), distancia)
            }
            .sortedBy { it.distanciaKm }
        return ResponseEntity.ok(cercanas)
    }

    @Operation(summary = "Servicios ofrecidos disponibles cerca de una coordenada", description = "Radio en kilometros (por defecto 15).")
    @GetMapping("/ofertas-cercanas")
    fun ofertasCercanas(
        @RequestParam lat: Double,
        @RequestParam lng: Double,
        @RequestParam(required = false, defaultValue = "15") radio: Double
    ): ResponseEntity<List<NearbyOfferedServiceDTO>> {
        val cercanas = offeredServiceService.available()
            .mapNotNull { offer ->
                val olat = offer.latitude ?: return@mapNotNull null
                val olng = offer.longitude ?: return@mapNotNull null
                val distancia = geocodingService.calcularDistanciaKm(lat, lng, olat, olng)
                if (distancia > radio) null else NearbyOfferedServiceDTO(OfferedServiceDTO.fromEntity(offer), distancia)
            }
            .sortedBy { it.distanciaKm }
        return ResponseEntity.ok(cercanas)
    }

    @Operation(summary = "Cuidadores con ubicacion registrada cerca de una coordenada", description = "Radio en kilometros (por defecto 15).")
    @GetMapping("/cuidadores-cercanos")
    fun cuidadoresCercanos(
        @RequestParam lat: Double,
        @RequestParam lng: Double,
        @RequestParam(required = false, defaultValue = "15") radio: Double
    ): ResponseEntity<List<NearbyCaregiverDTO>> {
        val cercanos = userRepository.findByRolAndLatitudIsNotNullAndLongitudIsNotNull("gestor")
            .mapNotNull { user -> userToNearby(user, lat, lng, radio) }
            .sortedBy { it.distanciaKm }
        return ResponseEntity.ok(cercanos)
    }

    private fun userToNearby(user: User, lat: Double, lng: Double, radio: Double): NearbyCaregiverDTO? {
        val ulat = user.latitud ?: return null
        val ulng = user.longitud ?: return null
        val distancia = geocodingService.calcularDistanciaKm(lat, lng, ulat, ulng)
        if (distancia > radio) return null
        val nombre = listOfNotNull(user.nombre, user.apellido).joinToString(" ").ifBlank { user.username }
        return NearbyCaregiverDTO(id = user.id ?: 0, nombre = nombre, distanciaKm = distancia)
    }
}
