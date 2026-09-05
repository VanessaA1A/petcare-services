package com.petcare.service

import com.petcare.model.Rating
import com.petcare.model.ServiceRequest
import com.petcare.repository.RatingRepository
import com.petcare.repository.ServiceRequestRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Optional

class RatingServiceTest {

    private val ratingRepository = mock<RatingRepository>()
    private val requestRepository = mock<ServiceRequestRepository>()
    private lateinit var ratingService: RatingService

    @BeforeEach
    fun setUp() {
        whenever(ratingRepository.save(any())) doAnswer { it.arguments[0] as Rating }
        ratingService = RatingService(ratingRepository, requestRepository)
    }

    private fun requestWithStatus(status: String) = ServiceRequest(id = 1, status = status)

    @Test
    fun `save rechaza si la solicitud no existe`() {
        whenever(requestRepository.findById(1)) doReturn Optional.empty()
        val rating = Rating(serviceRequestId = 1, caregiverId = 2, ownerId = 3)

        assertThrows(IllegalArgumentException::class.java) { ratingService.save(rating) }
    }

    @Test
    fun `save rechaza si la solicitud aun esta pendiente`() {
        whenever(requestRepository.findById(1)) doReturn Optional.of(requestWithStatus("PENDING"))
        val rating = Rating(serviceRequestId = 1, caregiverId = 2, ownerId = 3)

        assertThrows(IllegalArgumentException::class.java) { ratingService.save(rating) }
    }

    @Test
    fun `save acepta una solicitud completada`() {
        whenever(requestRepository.findById(1)) doReturn Optional.of(requestWithStatus("COMPLETED"))
        whenever(ratingRepository.findByServiceRequestIdAndRatedByRole(1, "OWNER")) doReturn null
        val rating = Rating(serviceRequestId = 1, caregiverId = 2, ownerId = 3, ratedByRole = "OWNER", score = 4.5)

        val saved = ratingService.save(rating)

        assertEquals(4.5, saved.score)
    }

    @Test
    fun `save actualiza la calificacion existente en vez de duplicarla`() {
        whenever(requestRepository.findById(1)) doReturn Optional.of(requestWithStatus("COMPLETED"))
        whenever(ratingRepository.findByServiceRequestIdAndRatedByRole(1, "OWNER")) doReturn
            Rating(id = 99, serviceRequestId = 1, caregiverId = 2, ownerId = 3, ratedByRole = "OWNER", score = 3.0)
        val nuevaCalificacion = Rating(serviceRequestId = 1, caregiverId = 2, ownerId = 3, ratedByRole = "OWNER", score = 5.0)

        val saved = ratingService.save(nuevaCalificacion)

        assertEquals(99, saved.id)
        assertEquals(5.0, saved.score)
    }

    @Test
    fun `caregiverSummary devuelve 5_0 y cero reseñas cuando no hay calificaciones`() {
        whenever(ratingRepository.findByCaregiverIdAndRatedByRole(2, "OWNER")) doReturn emptyList()

        val (promedio, cantidad) = ratingService.caregiverSummary(2)

        assertEquals(5.0, promedio)
        assertEquals(0, cantidad)
    }

    @Test
    fun `caregiverSummary calcula el promedio real cuando hay calificaciones`() {
        whenever(ratingRepository.findByCaregiverIdAndRatedByRole(2, "OWNER")) doReturn listOf(
            Rating(serviceRequestId = 1, caregiverId = 2, ownerId = 3, score = 4.0),
            Rating(serviceRequestId = 2, caregiverId = 2, ownerId = 4, score = 2.0)
        )

        val (promedio, cantidad) = ratingService.caregiverSummary(2)

        assertEquals(3.0, promedio)
        assertEquals(2, cantidad)
    }
}
