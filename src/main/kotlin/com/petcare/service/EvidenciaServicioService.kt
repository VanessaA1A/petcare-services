package com.petcare.service

/*
 * Comentario de modulo PetCare:
 * Servicio de negocio. Contiene reglas de PetCare que no deben vivir directamente en los controladores.
 */

import com.petcare.model.EvidenciaServicio
import com.petcare.repository.EvidenciaServicioRepository
import org.springframework.stereotype.Service

/**
 * Bloque 8 (foto obligatoria antes/despues). El requisito de "evidencia obligatoria" antes de
 * pasar a EN_PROGRESO (ACCEPTED en este esquema) y antes de completar el servicio es, por
 * diseno del producto, flexible: si el dispositivo no tiene internet, el cambio de estado se
 * permite igual y la foto se sube cuando reconecta. Por eso este servicio NO bloquea ninguna
 * transicion de estado (ver MobileServiceRequestService) - solo guarda evidencia y expone
 * `tieneEvidencia` para que la app (o un futuro reporte) sepa si un servicio quedo "sin
 * evidencia".
 */
@Service
class EvidenciaServicioService(
    private val repository: EvidenciaServicioRepository
) {
    fun guardar(evidencia: EvidenciaServicio): EvidenciaServicio = repository.save(evidencia)

    fun listar(solicitudId: Int): List<EvidenciaServicio> =
        repository.findBySolicitudIdOrderByFechaAsc(solicitudId)

    fun tieneEvidencia(solicitudId: Int, tipo: String): Boolean =
        repository.findBySolicitudIdAndTipo(solicitudId, tipo).isNotEmpty()
}
