package com.petcare.service

/*
 * Comentario de modulo PetCare:
 * Servicio de negocio. Contiene reglas de PetCare que no deben vivir directamente en los controladores.
 */

import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class GeocodeResult(val latitud: Double, val longitud: Double, val direccion: String)

@Service
class GeocodingService {
    // Nominatim exige un User-Agent identificable y limita a ~1 solicitud/segundo por IP.
    private val restClient = RestClient.builder()
        .baseUrl("https://nominatim.openstreetmap.org")
        .defaultHeader("User-Agent", "PetCareApp/1.0 (contacto@petcare.local)")
        .build()

    @Volatile
    private var lastCallAt: Long = 0

    @Synchronized
    private fun throttle() {
        val elapsed = System.currentTimeMillis() - lastCallAt
        if (elapsed in 0..1000) Thread.sleep(1000 - elapsed)
        lastCallAt = System.currentTimeMillis()
    }

    /** Convierte una direccion de texto en lat/lng, limitado a Nicaragua. Null si no se encontro. */
    @Suppress("UNCHECKED_CAST")
    fun geocode(direccion: String): GeocodeResult? {
        if (direccion.isBlank()) return null
        throttle()
        return try {
            val response = restClient.get()
                .uri { builder ->
                    builder.path("/search")
                        .queryParam("q", direccion)
                        .queryParam("format", "json")
                        .queryParam("countrycodes", "ni")
                        .queryParam("limit", "1")
                        .build()
                }
                .retrieve()
                .body(List::class.java) as? List<Map<String, Any?>>

            val first = response?.firstOrNull() ?: return null
            val lat = (first["lat"] as? String)?.toDoubleOrNull() ?: return null
            val lon = (first["lon"] as? String)?.toDoubleOrNull() ?: return null
            GeocodeResult(lat, lon, first["display_name"] as? String ?: direccion)
        } catch (ex: Exception) {
            null
        }
    }

    /** Distancia en kilometros entre dos coordenadas (formula de Haversine). */
    fun calcularDistanciaKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val radioTierraKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return radioTierraKm * c
    }
}
