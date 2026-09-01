package com.petcare.service

/*
 * Comentario de modulo PetCare:
 * Servicio de negocio. Contiene reglas de PetCare que no deben vivir directamente en los controladores.
 */

import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.time.Duration
import java.time.Instant

@Service
class DogBreedService {
    private val restClient = RestClient.create("https://dog.ceo")
    private val cacheTtl = Duration.ofHours(24)

    @Volatile
    private var cachedBreeds: List<String> = emptyList()

    @Volatile
    private var cachedAt: Instant? = null

    val siempreDisponibles = listOf("Mixto", "Desconocido", "Criollo", "No especificado")

    fun search(query: String?): List<String> {
        val breeds = allBreeds()
        val q = query?.trim().orEmpty()
        val filtered = if (q.isBlank()) {
            breeds
        } else {
            breeds.filter { it.contains(q, ignoreCase = true) }
        }
        val extras = siempreDisponibles.filter { q.isBlank() || it.contains(q, ignoreCase = true) }
        return (extras + filtered).distinct()
    }

    private fun allBreeds(): List<String> {
        val now = Instant.now()
        val isStale = cachedAt == null || Duration.between(cachedAt, now) > cacheTtl
        if (isStale) {
            fetchAndCache()
        }
        return cachedBreeds
    }

    @Suppress("UNCHECKED_CAST")
    private fun fetchAndCache() {
        try {
            val response = restClient.get()
                .uri("/api/breeds/list/all")
                .retrieve()
                .body(Map::class.java) as Map<String, Any?>

            val message = response["message"] as? Map<String, List<String>> ?: emptyMap()
            val breeds = mutableListOf<String>()
            message.forEach { (breed, subBreeds) ->
                if (subBreeds.isEmpty()) {
                    breeds += breed.replaceFirstChar { it.uppercase() }
                } else {
                    subBreeds.forEach { sub ->
                        breeds += "${sub.replaceFirstChar { it.uppercase() }} ${breed.replaceFirstChar { it.uppercase() }}"
                    }
                }
            }
            cachedBreeds = breeds.sorted()
            cachedAt = Instant.now()
        } catch (ex: Exception) {
            // Si Dog CEO no responde, se conserva el cache anterior (o lista vacia) en vez de romper el endpoint.
            if (cachedAt == null) {
                cachedBreeds = emptyList()
                cachedAt = Instant.now()
            }
        }
    }
}
