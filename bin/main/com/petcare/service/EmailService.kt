package com.petcare.service

/*
 * Comentario de modulo PetCare:
 * Servicio de negocio. Contiene reglas de PetCare que no deben vivir directamente en los controladores.
 */

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class EmailService(
    private val mailSender: JavaMailSender,
    @Value("\${spring.mail.host:}") private val mailHost: String,
    @Value("\${spring.mail.username:no-reply@petcare.local}") private val fromAddress: String
) {
    private val log = LoggerFactory.getLogger(EmailService::class.java)

    /** True mientras no haya un servidor SMTP configurado (sin credenciales de Gmail/SendGrid, etc). */
    val usaModoConsola: Boolean get() = mailHost.isBlank()

    /**
     * Envia el OTP por correo si hay SMTP configurado (spring.mail.host).
     * Sin credenciales SMTP (caso por defecto de este proyecto), el codigo solo se
     * registra en el log del servidor; el llamador decide si tambien lo devuelve
     * en la respuesta HTTP para poder probar el flujo sin un correo real.
     */
    fun sendOtp(email: String, otp: String) {
        if (usaModoConsola) {
            log.info("[MODO CONSOLA] Codigo de verificacion para {}: {}", email, otp)
            return
        }
        try {
            val message = SimpleMailMessage().apply {
                setFrom(fromAddress)
                setTo(email)
                setSubject("Tu código de verificación de PetCare")
                setText("Tu código de verificación es: $otp\nVence en 5 minutos.")
            }
            mailSender.send(message)
        } catch (ex: Exception) {
            // Si el SMTP configurado falla, no se pierde el codigo: queda en el log del servidor.
            log.warn("No se pudo enviar el correo con el OTP, revisa la config de SMTP. Codigo para {}: {}", email, otp, ex)
        }
    }
}
