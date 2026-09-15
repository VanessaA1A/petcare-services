package com.petcare.websocket

/*
 * Comentario de modulo PetCare:
 * Soporte WebSocket. Guarda en memoria la ultima ubicacion conocida de cada
 * solicitud de servicio en curso (taxi/paseo) para la funcion de mapa en vivo.
 */

import org.springframework.stereotype.Component
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

data class LiveLocation(val latitude: Double, val longitude: Double, val updatedAt: Instant)

@Component
class LiveLocationRegistry {

    private val locationsByRequest = ConcurrentHashMap<Int, LiveLocation>()

    fun update(serviceRequestId: Int, latitude: Double, longitude: Double) {
        locationsByRequest[serviceRequestId] = LiveLocation(latitude, longitude, Instant.now())
    }

    fun get(serviceRequestId: Int): LiveLocation? = locationsByRequest[serviceRequestId]
}
