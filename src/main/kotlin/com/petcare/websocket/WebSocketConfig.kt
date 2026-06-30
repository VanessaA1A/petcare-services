package com.petcare.websocket

/*
 * Comentario de modulo PetCare:
 * Soporte WebSocket. Maneja sesiones y eventos enviados en tiempo real a la app.
 */

import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
class WebSocketConfig(
    private val petCareWebSocketHandler: PetCareWebSocketHandler
) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry
            .addHandler(petCareWebSocketHandler, "/ws/petcare")
            .setAllowedOrigins("*")
    }
}