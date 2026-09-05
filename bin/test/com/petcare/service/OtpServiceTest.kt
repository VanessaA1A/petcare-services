package com.petcare.service

import com.petcare.model.Verificacion
import com.petcare.repository.VerificacionRepository
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.OffsetDateTime

class OtpServiceTest {

    private val repository = mock<VerificacionRepository>()
    private val emailService = mock<EmailService>()
    private lateinit var otpService: OtpService

    @BeforeEach
    fun setUp() {
        whenever(emailService.usaModoConsola) doReturn true
        whenever(repository.save(any())) doAnswer { it.arguments[0] as Verificacion }
        otpService = OtpService(repository, emailService)
    }

    @Test
    fun `generarYEnviar devuelve el otp cuando no hay SMTP configurado`() {
        val otp = otpService.generarYEnviar("dueno@petcare.local")

        assertTrue(otp != null && otp.length == 6)
    }

    @Test
    fun `generarYEnviar no devuelve el otp cuando si hay SMTP configurado`() {
        whenever(emailService.usaModoConsola) doReturn false

        val otp = otpService.generarYEnviar("dueno@petcare.local")

        assertTrue(otp == null)
    }

    @Test
    fun `verificar acepta un codigo vigente y no usado, y lo marca como usado`() {
        val verificacion = Verificacion(
            email = "dueno@petcare.local",
            otp = "123456",
            fechaExpiracion = OffsetDateTime.now().plusMinutes(5),
            usado = false
        )
        whenever(repository.findTopByEmailAndUsadoFalseOrderByCreadoEnDesc("dueno@petcare.local")) doReturn verificacion

        val resultado = otpService.verificar("dueno@petcare.local", "123456")

        assertTrue(resultado)
        assertTrue(verificacion.usado)
    }

    @Test
    fun `verificar rechaza un codigo incorrecto`() {
        val verificacion = Verificacion(
            email = "dueno@petcare.local",
            otp = "123456",
            fechaExpiracion = OffsetDateTime.now().plusMinutes(5),
            usado = false
        )
        whenever(repository.findTopByEmailAndUsadoFalseOrderByCreadoEnDesc("dueno@petcare.local")) doReturn verificacion

        assertFalse(otpService.verificar("dueno@petcare.local", "000000"))
    }

    @Test
    fun `verificar rechaza un codigo expirado`() {
        val verificacion = Verificacion(
            email = "dueno@petcare.local",
            otp = "123456",
            fechaExpiracion = OffsetDateTime.now().minusMinutes(1),
            usado = false
        )
        whenever(repository.findTopByEmailAndUsadoFalseOrderByCreadoEnDesc("dueno@petcare.local")) doReturn verificacion

        assertFalse(otpService.verificar("dueno@petcare.local", "123456"))
    }

    @Test
    fun `verificar rechaza cuando no hay ningun codigo pendiente`() {
        whenever(repository.findTopByEmailAndUsadoFalseOrderByCreadoEnDesc("nadie@petcare.local")) doReturn null

        assertFalse(otpService.verificar("nadie@petcare.local", "123456"))
    }
}
