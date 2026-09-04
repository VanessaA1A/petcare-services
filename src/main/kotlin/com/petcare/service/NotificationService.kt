package com.petcare.service

/*
 * Comentario de modulo PetCare:
 * Servicio de negocio. Envia notificaciones push (Firebase Cloud Messaging) a los usuarios.
 * Es tolerante a la ausencia de Firebase o de token FCM: si falta alguno, registra
 * la situacion y no hace nada (no lanza excepciones que interrumpan el flujo que dispara el envio).
 */

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import com.petcare.config.FirebaseConfig
import com.petcare.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class NotificationService(
    private val userRepository: UserRepository
) {
    private val logger = LoggerFactory.getLogger(NotificationService::class.java)

    fun sendNotificationToUser(usuarioId: Int, titulo: String, cuerpo: String, data: Map<String, String> = emptyMap()) {
        if (!FirebaseConfig.isInitialized()) {
            logger.warn("Firebase no esta inicializado: se omite el envio de notificacion push a usuario {}", usuarioId)
            return
        }

        val user = userRepository.findById(usuarioId).orElse(null)
        val token = user?.fcmToken
        if (token.isNullOrBlank()) {
            logger.info("Usuario {} no tiene token FCM registrado: se omite el envio de notificacion push", usuarioId)
            return
        }

        try {
            val message = Message.builder()
                .setToken(token)
                .setNotification(
                    Notification.builder()
                        .setTitle(titulo)
                        .setBody(cuerpo)
                        .build()
                )
                .putAllData(data)
                .build()

            val response = FirebaseMessaging.getInstance().send(message)
            logger.info("Notificacion push enviada a usuario {}: {}", usuarioId, response)
        } catch (ex: Exception) {
            // Un error de envio (token invalido/expirado, red, etc.) no debe interrumpir el flujo principal.
            logger.warn("No se pudo enviar la notificacion push a usuario {}: {}", usuarioId, ex.message)
        }
    }

    fun saveFcmToken(usuarioId: Int, token: String) {
        val user = userRepository.findById(usuarioId).orElse(null)
        if (user == null) {
            logger.warn("No se pudo guardar el token FCM: usuario {} no encontrado", usuarioId)
            return
        }
        user.fcmToken = token
        userRepository.save(user)
    }
}
