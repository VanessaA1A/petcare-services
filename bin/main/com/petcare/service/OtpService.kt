package com.petcare.service

/*
 * Comentario de modulo PetCare:
 * Servicio de negocio. Contiene reglas de PetCare que no deben vivir directamente en los controladores.
 */

import com.petcare.model.Verificacion
import com.petcare.repository.VerificacionRepository
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import kotlin.random.Random

@Service
class OtpService(
    private val repository: VerificacionRepository,
    private val emailService: EmailService
) {
    /** Genera, guarda y "envia" un OTP de 6 digitos valido por 5 minutos. Devuelve el codigo en modo consola. */
    fun generarYEnviar(email: String): String? {
        val otp = Random.nextInt(0, 1_000_000).toString().padStart(6, '0')
        repository.save(
            Verificacion(
                email = email,
                otp = otp,
                fechaExpiracion = OffsetDateTime.now().plusMinutes(5)
            )
        )
        emailService.sendOtp(email, otp)
        return if (emailService.usaModoConsola) otp else null
    }

    fun verificar(email: String, otp: String): Boolean {
        val verificacion = repository.findTopByEmailAndUsadoFalseOrderByCreadoEnDesc(email) ?: return false
        val vigente = verificacion.fechaExpiracion?.isAfter(OffsetDateTime.now()) == true
        if (!vigente || verificacion.otp != otp) return false
        verificacion.usado = true
        repository.save(verificacion)
        return true
    }
}
