package com.petcare.repository

import com.petcare.model.Activity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ActivityRepository : JpaRepository<Activity, UUID>
