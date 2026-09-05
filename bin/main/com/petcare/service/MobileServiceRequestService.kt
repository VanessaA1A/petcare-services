package com.petcare.service

/*
 * Comentario de modulo PetCare:
 * Servicio de negocio. Contiene reglas de PetCare que no deben vivir directamente en los controladores.
 */

import com.petcare.model.ServiceApplication
import com.petcare.model.ServiceRequest
import com.petcare.repository.OfferedServiceRepository
import com.petcare.repository.ServiceApplicationRepository
import com.petcare.repository.ServiceRequestRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import jakarta.persistence.EntityManager
import jakarta.persistence.criteria.Predicate

@Service
class MobileServiceRequestService(
    private val requestRepository: ServiceRequestRepository,
    private val applicationRepository: ServiceApplicationRepository,
    private val offeredServiceRepository: OfferedServiceRepository,
    private val entityManager: EntityManager
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

    @Transactional
    fun updateRequest(id: Int, update: ServiceRequest): ServiceRequest? {
        val existing = requestRepository.findById(id).orElse(null) ?: return null
        if (existing.status != "PENDING") {
            throw IllegalStateException("Solo se pueden editar solicitudes en estado PENDING")
        }
        
        existing.title = update.title ?: existing.title
        existing.description = update.description ?: existing.description
        existing.requestedDate = update.requestedDate ?: existing.requestedDate
        existing.startTime = update.startTime ?: existing.startTime
        existing.endTime = update.endTime ?: existing.endTime
        existing.petId = update.petId ?: existing.petId
        existing.petIds = update.petIds ?: existing.petIds
        existing.serviceTypeId = update.serviceTypeId ?: existing.serviceTypeId
        existing.latitude = update.latitude ?: existing.latitude
        existing.longitude = update.longitude ?: existing.longitude
        
        return requestRepository.save(existing)
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

    @Transactional
    fun markDoneByCaregiver(id: Int): ServiceApplication? {
        val application = applicationRepository.findById(id).orElse(null) ?: return null
        if (application.status != "ACCEPTED") return null

        application.status = "DONE_BY_CAREGIVER"
        val saved = applicationRepository.save(application)

        requestRepository.findById(application.serviceRequestId ?: -1).ifPresent { request ->
            request.status = "DONE_BY_CAREGIVER"
            requestRepository.save(request)
        }

        return saved
    }

    @Transactional
    fun cancelApplication(id: Int, reason: String? = null): ServiceApplication? {
        val application = applicationRepository.findById(id).orElse(null) ?: return null
        
        if (application.status == "PENDING") {
            application.status = "CANCELLED"
            return applicationRepository.save(application)
        }

        if (application.status != "ACCEPTED") {
            throw CancellationNotAllowedException("Solo se pueden cancelar servicios pendientes o aceptados.")
        }

        val request = requestRepository.findById(application.serviceRequestId ?: -1).orElse(null)
            ?: throw CancellationNotAllowedException("No se encontró la solicitud asociada.")

        if (!ServiceDateTimeParser.canCancelBeforeStart(request)) {
            throw CancellationNotAllowedException("No se puede cancelar tan cerca de la hora de inicio.")
        }

        application.status = "CANCELLED"
        val saved = applicationRepository.save(application)
        request.status = "CANCELLED"
        request.motivoCancelacion = reason
        requestRepository.save(request)
        return saved
    }

    fun completeApplication(id: Int): ServiceApplication? {
        val application = applicationRepository.findById(id).orElse(null) ?: return null
        if (application.status != "ACCEPTED" && application.status != "DONE_BY_CAREGIVER") return null

        application.status = "COMPLETED"
        val saved = applicationRepository.save(application)
        requestRepository.findById(application.serviceRequestId ?: -1).ifPresent { request ->
            request.status = "COMPLETED"
            requestRepository.save(request)
        }
        return saved
    }

    @Transactional
    fun extendRequest(id: Int): ServiceRequest? {
        val request = requestRepository.findById(id).orElse(null) ?: return null
        if (request.status != "PENDING") return null
        
        val baseDate = request.fechaExpiracion ?: OffsetDateTime.now()
        request.fechaExpiracion = baseDate.plusHours(24)
        return requestRepository.save(request)
    }

    @Transactional
    fun reassignRequest(id: Int): ServiceRequest? {
        val request = requestRepository.findById(id).orElse(null) ?: return null
        if (request.status != "CANCELLED" && request.status != "ACCEPTED") return null
        
        request.status = "PENDING"
        requestRepository.save(request)
        
        applicationRepository.findByServiceRequestIdOrderByCreatedAtDesc(id)
            .filter { it.status == "ACCEPTED" }
            .forEach { applicationRepository.save(it.copy(status = "CANCELLED")) }
            
        return request
    }

    fun getHistory(userId: Int, role: String): List<ServiceRequest> {
        return if (role.uppercase() == "OWNER") {
            requestRepository.findByOwnerIdOrderByCreatedAtDesc(userId)
                .filter { it.status == "COMPLETED" || it.status == "CANCELLED" }
        } else {
            val requestIds = applicationRepository.findByCaregiverIdOrderByCreatedAtDesc(userId)
                .filter { it.status == "COMPLETED" || it.status == "CANCELLED" }
                .mapNotNull { it.serviceRequestId }
            requestRepository.findAllById(requestIds).sortedByDescending { it.createdAt }
        }
    }

    fun searchRequests(query: String?, serviceTypeId: Int?, status: String?): List<ServiceRequest> {
        val cb = entityManager.criteriaBuilder
        val cq = cb.createQuery(ServiceRequest::class.java)
        val root = cq.from(ServiceRequest::class.java)
        val predicates = mutableListOf<Predicate>()

        if (!query.isNullOrBlank()) {
            val q = "%${query.lowercase()}%"
            predicates.add(cb.or(
                cb.like(cb.lower(root.get("title")), q),
                cb.like(cb.lower(root.get("description")), q)
            ))
        }

        if (serviceTypeId != null && serviceTypeId > 0) {
            predicates.add(cb.equal(root.get<Int>("serviceTypeId"), serviceTypeId))
        }

        if (!status.isNullOrBlank()) {
            predicates.add(cb.equal(cb.upper(root.get("status")), status.uppercase()))
        } else {
            predicates.add(cb.equal(root.get<String>("status"), "PENDING"))
        }

        cq.where(*predicates.toTypedArray())
        cq.orderBy(cb.desc(root.get<OffsetDateTime>("createdAt")))

        return entityManager.createQuery(cq).resultList
    }
}
