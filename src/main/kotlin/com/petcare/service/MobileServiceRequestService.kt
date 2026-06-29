package com.petcare.service

import com.petcare.model.ServiceApplication
import com.petcare.model.ServiceRequest
import com.petcare.repository.OfferedServiceRepository
import com.petcare.repository.ServiceApplicationRepository
import com.petcare.repository.ServiceRequestRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MobileServiceRequestService(
    private val requestRepository: ServiceRequestRepository,
    private val applicationRepository: ServiceApplicationRepository,
    private val offeredServiceRepository: OfferedServiceRepository
) {
    fun byOwner(ownerId: Int) = requestRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId)
    fun available() = requestRepository.findByStatusIgnoreCaseAndSourceTypeIgnoreCaseOrderByCreatedAtDesc("PENDING", "OPEN")
    fun findRequest(id: Int) = requestRepository.findById(id)
    fun saveRequest(request: ServiceRequest) = requestRepository.save(request)

    @Transactional
    fun createRequest(request: ServiceRequest): ServiceRequest {
        if ((request.id ?: 0) <= 0) request.id = generateRequestId()
        request.status = request.status.ifBlank { "PENDING" }.uppercase()
        request.sourceType = request.sourceType.ifBlank { "OPEN" }.uppercase()

        val saved = requestRepository.save(request)
        createOwnerInitiatedApplicationIfNeeded(saved)
        return saved
    }


    private fun createOwnerInitiatedApplicationIfNeeded(request: ServiceRequest) {
        if (request.sourceType != "OFFER") return
        val requestId = request.id ?: return
        val offeredServiceId = request.offeredServiceId ?: return
        val offer = offeredServiceRepository.findById(offeredServiceId).orElse(null) ?: return
        val caregiverId = offer.caregiverId ?: return

        val existing = applicationRepository.findByServiceRequestIdAndCaregiverId(requestId, caregiverId)
        if (existing != null) return

        applicationRepository.save(
            ServiceApplication(
                serviceRequestId = requestId,
                caregiverId = caregiverId,
                offeredServiceId = offeredServiceId,
                initiatedBy = "OWNER",
                status = "PENDING"
            )
        )
    }

    private fun generateRequestId(): Int {
        var candidate = (System.currentTimeMillis() % Int.MAX_VALUE).toInt().let { if (it <= 0) 1 else it }
        while (requestRepository.existsById(candidate)) {
            candidate = if (candidate == Int.MAX_VALUE) 1 else candidate + 1
        }
        return candidate
    }

    fun applicationsByCaregiver(caregiverId: Int) = applicationRepository.findByCaregiverIdOrderByCreatedAtDesc(caregiverId)
    fun applicationsByOwner(ownerId: Int): List<ServiceApplication> {
        val requestIds = byOwner(ownerId).mapNotNull { it.id }
        if (requestIds.isEmpty()) return emptyList()
        return applicationRepository.findByServiceRequestIdInOrderByCreatedAtDesc(requestIds)
    }

    fun saveApplication(application: ServiceApplication): ServiceApplication {
        val existing = applicationRepository.findByServiceRequestIdAndCaregiverId(
            application.serviceRequestId ?: -1,
            application.caregiverId ?: -1
        )
        return applicationRepository.save(application.copy(id = existing?.id ?: application.id))
    }

    fun findApplication(id: Int) = applicationRepository.findById(id)

    @Transactional
    fun acceptApplication(id: Int): ServiceApplication? {
        val application = applicationRepository.findById(id).orElse(null) ?: return null
        if (application.status != "PENDING") return null

        application.status = "ACCEPTED"
        val saved = applicationRepository.save(application)

        val request = requestRepository.findById(application.serviceRequestId ?: -1).orElse(null)
        if (request != null) {
            request.status = "ACCEPTED"
            requestRepository.save(request)

            applicationRepository.findByServiceRequestIdOrderByCreatedAtDesc(request.id ?: -1)
                .filter { it.id != saved.id && it.status == "PENDING" }
                .forEach { other -> applicationRepository.save(other.copy(status = "REJECTED")) }
        }

        return saved
    }

    fun rejectApplication(id: Int): ServiceApplication? {
        val application = applicationRepository.findById(id).orElse(null) ?: return null
        if (application.status != "PENDING") return null
        application.status = "REJECTED"
        return applicationRepository.save(application)
    }

    fun cancelApplication(id: Int): ServiceApplication? {
        val application = applicationRepository.findById(id).orElse(null) ?: return null
        if (application.status != "ACCEPTED") {
            throw CancellationNotAllowedException("Solo se pueden cancelar servicios confirmados.")
        }

        val request = requestRepository.findById(application.serviceRequestId ?: -1).orElse(null)
            ?: throw CancellationNotAllowedException("No se encontró la solicitud asociada.")

        if (!ServiceDateTimeParser.canCancelBeforeStart(request)) {
            throw CancellationNotAllowedException()
        }

        application.status = "CANCELLED"
        val saved = applicationRepository.save(application)
        request.status = "CANCELLED"
        requestRepository.save(request)
        return saved
    }

    fun completeApplication(id: Int): ServiceApplication? {
        val application = applicationRepository.findById(id).orElse(null) ?: return null
        if (application.status != "ACCEPTED") return null

        application.status = "COMPLETED"
        val saved = applicationRepository.save(application)
        requestRepository.findById(application.serviceRequestId ?: -1).ifPresent { request ->
            request.status = "COMPLETED"
            requestRepository.save(request)
        }
        return saved
    }
}
