package com.petcare.controller

import com.petcare.dto.ServiceApplicationDTO
import com.petcare.dto.ServiceRequestDTO
import com.petcare.service.CancellationNotAllowedException
import com.petcare.service.MobileServiceRequestService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/service-requests")
class MobileServiceRequestsController(private val service: MobileServiceRequestService) {
    @GetMapping("/owner/{ownerId}")
    fun byOwner(@PathVariable ownerId: Int) = ResponseEntity.ok(
        service.byOwner(ownerId).map { ServiceRequestDTO.fromEntity(it) }
    )

    @GetMapping("/available")
    fun available() = ResponseEntity.ok(
        service.available().map { ServiceRequestDTO.fromEntity(it) }
    )

    @GetMapping("/{id}")
    fun byId(@PathVariable id: Int): ResponseEntity<*> {
        val request = service.findRequest(id)
        return if (request.isPresent) ResponseEntity.ok(ServiceRequestDTO.fromEntity(request.get()))
        else ResponseEntity.status(404).body(mapOf("error" to "Service request not found"))
    }

    @PostMapping
    fun create(@RequestBody body: Map<String, Any?>): ResponseEntity<*> {
        val request = body.toServiceRequestDTO()
        if (request.ownerId <= 0 || request.petId <= 0 || request.serviceTypeId <= 0 || request.title.isBlank()) {
            return ResponseEntity.badRequest().body(mapOf("error" to "owner_id, pet_id, service_type_id and title are required"))
        }
        val saved = service.createRequest(request.toEntity())
        return ResponseEntity.status(201).body(ServiceRequestDTO.fromEntity(saved))
    }

    @PutMapping("/{id}/status")
    fun updateStatus(@PathVariable id: Int, @RequestBody request: Map<String, String>): ResponseEntity<*> {
        val existing = service.findRequest(id)
        if (existing.isEmpty) return ResponseEntity.status(404).body(mapOf("error" to "Service request not found"))
        val entity = existing.get()
        entity.status = request["status"].orEmpty().uppercase()
        return ResponseEntity.ok(ServiceRequestDTO.fromEntity(service.saveRequest(entity)))
    }

    @PutMapping("/{id}/schedule")
    fun updateSchedule(@PathVariable id: Int, @RequestBody request: Map<String, Any?>): ResponseEntity<*> {
        val existing = service.findRequest(id)
        if (existing.isEmpty) return ResponseEntity.status(404).body(mapOf("error" to "Service request not found"))
        val entity = existing.get()
        entity.requestedDate = request.stringValue("requested_date", "requestedDate")
        entity.startTime = request.stringValue("start_time", "startTime")
        entity.endTime = request.stringValue("end_time", "endTime")
        return ResponseEntity.ok(ServiceRequestDTO.fromEntity(service.saveRequest(entity)))
    }
}

@RestController
@RequestMapping("/api/service-applications")
class MobileServiceApplicationsController(private val service: MobileServiceRequestService) {
    @GetMapping("/caregiver/{caregiverId}")
    fun byCaregiver(@PathVariable caregiverId: Int) = ResponseEntity.ok(
        service.applicationsByCaregiver(caregiverId).map { ServiceApplicationDTO.fromEntity(it) }
    )

    @GetMapping("/owner/{ownerId}")
    fun byOwner(@PathVariable ownerId: Int) = ResponseEntity.ok(
        service.applicationsByOwner(ownerId).map { ServiceApplicationDTO.fromEntity(it) }
    )

    @PostMapping
    fun create(@RequestBody body: Map<String, Any?>): ResponseEntity<*> {
        val request = body.toServiceApplicationDTO()
        if (request.serviceRequestId <= 0 || request.caregiverId <= 0) {
            return ResponseEntity.badRequest().body(mapOf("error" to "service_request_id and caregiver_id are required"))
        }
        val saved = service.saveApplication(request.toEntity())
        return ResponseEntity.status(201).body(ServiceApplicationDTO.fromEntity(saved))
    }

    @PutMapping("/{id}/status")
    fun updateStatus(@PathVariable id: Int, @RequestBody request: Map<String, String>): ResponseEntity<*> {
        return try {
            val saved = when (request["status"].orEmpty().uppercase()) {
                "ACCEPTED" -> service.acceptApplication(id)
                "REJECTED" -> service.rejectApplication(id)
                "CANCELLED" -> service.cancelApplication(id)
                "COMPLETED" -> service.completeApplication(id)
                else -> {
                    val existing = service.findApplication(id)
                    if (existing.isEmpty) return ResponseEntity.status(404).body(mapOf("error" to "Service application not found"))
                    val entity = existing.get()
                    entity.status = request["status"].orEmpty().uppercase()
                    service.saveApplication(entity)
                }
            }
            if (saved != null) ResponseEntity.ok(ServiceApplicationDTO.fromEntity(saved))
            else ResponseEntity.status(404).body(mapOf("error" to "Service application not found"))
        } catch (ex: CancellationNotAllowedException) {
            ResponseEntity.badRequest().body(mapOf("error" to ex.message))
        }
    }
}


private fun Map<String, Any?>.toServiceRequestDTO() = ServiceRequestDTO(
    id = intValue("id"),
    ownerId = intValue("owner_id", "ownerId"),
    petId = intValue("pet_id", "petId"),
    petIds = intListValue("pet_ids", "petIds"),
    serviceTypeId = intValue("service_type_id", "serviceTypeId"),
    title = stringValue("title").orEmpty(),
    description = stringValue("description"),
    requestedDate = stringValue("requested_date", "requestedDate"),
    startTime = stringValue("start_time", "startTime"),
    endTime = stringValue("end_time", "endTime"),
    status = stringValue("status")?.uppercase() ?: "PENDING",
    offeredServiceId = nullableIntValue("offered_service_id", "offeredServiceId"),
    sourceType = stringValue("source_type", "sourceType")?.uppercase() ?: "OPEN",
    latitude = doubleValue("latitude"),
    longitude = doubleValue("longitude")
)

private fun Map<String, Any?>.toServiceApplicationDTO() = ServiceApplicationDTO(
    id = nullableIntValue("id"),
    serviceRequestId = intValue("service_request_id", "serviceRequestId"),
    caregiverId = intValue("caregiver_id", "caregiverId"),
    offeredServiceId = nullableIntValue("offered_service_id", "offeredServiceId"),
    initiatedBy = stringValue("initiated_by", "initiatedBy")?.uppercase() ?: "CAREGIVER",
    status = stringValue("status")?.uppercase() ?: "PENDING"
)

private fun Map<String, Any?>.value(vararg keys: String): Any? = keys.firstNotNullOfOrNull { this[it] }
private fun Map<String, Any?>.stringValue(vararg keys: String): String? = value(*keys)?.toString()?.takeIf { it.isNotBlank() }
private fun Map<String, Any?>.intValue(vararg keys: String): Int = nullableIntValue(*keys) ?: 0
private fun Map<String, Any?>.nullableIntValue(vararg keys: String): Int? = when (val raw = value(*keys)) {
    is Number -> raw.toInt()
    is String -> raw.toIntOrNull()
    else -> null
}
private fun Map<String, Any?>.doubleValue(vararg keys: String): Double? = when (val raw = value(*keys)) {
    is Number -> raw.toDouble()
    is String -> raw.toDoubleOrNull()
    else -> null
}
private fun Map<String, Any?>.intListValue(vararg keys: String): List<Int> {
    val raw = value(*keys) ?: return emptyList()
    return when (raw) {
        is Collection<*> -> raw.mapNotNull {
            when (it) {
                is Number -> it.toInt()
                is String -> it.toIntOrNull()
                else -> null
            }
        }
        is String -> raw.split(',').mapNotNull { it.trim().toIntOrNull() }
        else -> emptyList()
    }
}
