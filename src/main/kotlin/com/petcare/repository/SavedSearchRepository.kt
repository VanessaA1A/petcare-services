package com.petcare.repository

import com.petcare.model.SavedSearch
import org.springframework.data.jpa.repository.JpaRepository

interface SavedSearchRepository : JpaRepository<SavedSearch, Int> {
    fun findByUsuarioId(usuarioId: Int): List<SavedSearch>
}
