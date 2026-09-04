package com.petcare.repository

import com.petcare.model.Favorite
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface FavoriteRepository : JpaRepository<Favorite, Int> {
    fun findByUsuarioId(usuarioId: Int): List<Favorite>
    fun findByUsuarioIdAndCaregiverId(usuarioId: Int, caregiverId: Int): Optional<Favorite>
    fun findByUsuarioIdAndPetId(usuarioId: Int, petId: Int): Optional<Favorite>
}
