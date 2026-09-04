package com.petcare.config

/*
 * Comentario de modulo PetCare:
 * Configuracion de Firebase Admin SDK para notificaciones push (FCM).
 * Es tolerante a la ausencia de credenciales: si el archivo de servicio no existe,
 * registra una advertencia y NO inicializa Firebase, en vez de detener el arranque de Spring.
 */

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File
import java.io.FileInputStream

@Component
class FirebaseConfig(
    @Value("\${firebase.service-account-path:src/main/resources/firebase-service-account.json}")
    private val serviceAccountPath: String
) {
    private val logger = LoggerFactory.getLogger(FirebaseConfig::class.java)

    @PostConstruct
    fun initialize() {
        val path = System.getenv("FIREBASE_SERVICE_ACCOUNT_PATH") ?: serviceAccountPath
        val credentialsFile = File(path)

        if (!credentialsFile.exists()) {
            logger.warn(
                "Firebase no configurado: coloca el archivo de credenciales en {} para habilitar notificaciones push",
                path
            )
            return
        }

        try {
            if (FirebaseApp.getApps().isEmpty()) {
                FileInputStream(credentialsFile).use { stream ->
                    val options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(stream))
                        .build()
                    FirebaseApp.initializeApp(options)
                    logger.info("Firebase Admin SDK inicializado correctamente desde {}", path)
                }
            }
        } catch (ex: Exception) {
            // No se debe tumbar el contexto de Spring por un problema de credenciales invalidas.
            logger.warn("No se pudo inicializar Firebase Admin SDK: {}", ex.message)
        }
    }

    companion object {
        fun isInitialized(): Boolean = FirebaseApp.getApps().isNotEmpty()
    }
}
