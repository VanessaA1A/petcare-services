package com.petcare.service

import com.petcare.model.ServiceRequest
import java.text.SimpleDateFormat
import java.util.Locale

object ServiceDateTimeParser {
    private const val CANCELLATION_WINDOW_MS = 3L * 60L * 60L * 1000L

    fun parseStartMillis(request: ServiceRequest): Long? {
        val date = request.requestedDate?.trim().orEmpty()
        if (date.isEmpty()) return null

        val time = request.startTime?.trim().orEmpty()
        val hasTime = time.isNotEmpty()
        val pattern = if (hasTime) "dd/MM/yyyy HH:mm" else "dd/MM/yyyy"
        val text = if (hasTime) "$date $time" else date

        return try {
            SimpleDateFormat(pattern, Locale.getDefault())
                .apply { isLenient = false }
                .parse(text)
                ?.time
        } catch (_: Exception) {
            null
        }
    }

    fun canCancelBeforeStart(request: ServiceRequest, nowMillis: Long = System.currentTimeMillis()): Boolean {
        val startMillis = parseStartMillis(request) ?: return true
        return nowMillis <= startMillis - CANCELLATION_WINDOW_MS
    }
}
