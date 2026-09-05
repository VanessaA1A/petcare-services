package com.petcare.repository

import com.petcare.model.UserNote
import org.springframework.data.jpa.repository.JpaRepository

interface UserNoteRepository : JpaRepository<UserNote, Int> {
    fun findByOwnerId(ownerId: Int): List<UserNote>
    fun findByOwnerIdAndTargetId(ownerId: Int, targetId: Int): List<UserNote>
}
