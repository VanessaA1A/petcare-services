package com.petcare.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GeocodingServiceTest {

    private val service = GeocodingService()

    @Test
    fun `la distancia entre el mismo punto es cero`() {
        val distancia = service.calcularDistanciaKm(12.1364, -86.2514, 12.1364, -86.2514)
        assertEquals(0.0, distancia, 0.0001)
    }

    @Test
    fun `un grado de latitud equivale aproximadamente a 111 km`() {
        val distancia = service.calcularDistanciaKm(0.0, 0.0, 1.0, 0.0)
        assertTrue(distancia in 110.0..112.0, "Se esperaba ~111km, fue $distancia")
    }

    @Test
    fun `la distancia es simetrica sin importar el orden de los puntos`() {
        val ab = service.calcularDistanciaKm(12.1364, -86.2514, 12.4340, -86.8780)
        val ba = service.calcularDistanciaKm(12.4340, -86.8780, 12.1364, -86.2514)
        assertEquals(ab, ba, 0.0001)
    }
}
