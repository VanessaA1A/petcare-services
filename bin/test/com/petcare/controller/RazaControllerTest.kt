package com.petcare.controller

import com.petcare.service.DogBreedService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

// addFilters = false: esta prueba cubre el controlador, no las reglas de SecurityConfig
// (que @WebMvcTest no carga; sin esto Spring Boot aplica su seguridad por defecto y todo da 401).
@WebMvcTest(RazaController::class)
@AutoConfigureMockMvc(addFilters = false)
class RazaControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var dogBreedService: DogBreedService

    @Test
    fun `GET api-razas devuelve la lista que entrega el servicio`() {
        org.mockito.BDDMockito.given(dogBreedService.search("labra"))
            .willReturn(listOf("Labrador Retriever", "Mixto"))

        mockMvc.perform(get("/api/razas").param("q", "labra"))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.razas[0]").value("Labrador Retriever"))
            .andExpect(jsonPath("$.razas[1]").value("Mixto"))
    }

    @Test
    fun `GET api-razas sin query tambien responde 200`() {
        org.mockito.BDDMockito.given(dogBreedService.search(null))
            .willReturn(listOf("Mixto", "Desconocido", "Criollo", "No especificado"))

        mockMvc.perform(get("/api/razas"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.razas.length()").value(4))
    }
}
