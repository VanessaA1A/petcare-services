package com.petcare.dto

/*
 * Comentario de modulo PetCare:
 * Objeto de transferencia. Define la forma de los datos que entran o salen por la API.
 */

import com.fasterxml.jackson.annotation.JsonProperty
import com.petcare.model.AlertaPerdida
import com.petcare.model.Avistamiento
import io.swagger.v3.oas.annotations.media.Schema

data class AlertaPerdidaDTO(
    @Schema(example = "7") val id: Int? = null,
    @JsonProperty("pets_id") @Schema(example = "4") val petsId: Int,
    @JsonProperty("usuario_id") @Schema(example = "17") val usuarioId: Int,
    @Schema(example = "Se escapó por el portón, es muy asustadiza") val descripcion: String? = null,
    @Schema(example = "12.1364") val latitud: Double,
    @Schema(example = "-86.2514") val longitud: Double,
    @JsonProperty("direccion_texto") @Schema(example = "Barrio Monseñor Lezcano, Managua") val direccionTexto: String? = null,
    @Schema(example = "ACTIVA", description = "ACTIVA, ENCONTRADA o CERRADA") val estado: String = "ACTIVA",
    @JsonProperty("fecha_creacion") @Schema(example = "2026-09-15T10:00:00") val fechaCreacion: String? = null,
    @JsonProperty("fecha_cierre") val fechaCierre: String? = null
) {
    companion object {
        fun fromEntity(entity: AlertaPerdida) = AlertaPerdidaDTO(
            id = entity.id,
            petsId = entity.petsId ?: 0,
            usuarioId = entity.usuarioId ?: 0,
            descripcion = entity.descripcion,
            latitud = entity.latitud ?: 0.0,
            longitud = entity.longitud ?: 0.0,
            direccionTexto = entity.direccionTexto,
            estado = entity.estado,
            fechaCreacion = entity.fechaCreacion?.toString(),
            fechaCierre = entity.fechaCierre?.toString()
        )
    }

    fun toEntity(): AlertaPerdida = AlertaPerdida(
        petsId = petsId,
        usuarioId = usuarioId,
        descripcion = descripcion,
        latitud = latitud,
        longitud = longitud,
        direccionTexto = direccionTexto
    )
}

data class AlertaPerdidaCercanaDTO(
    val alerta: AlertaPerdidaDTO,
    @JsonProperty("distancia_km") val distanciaKm: Double
)

data class AvistamientoDTO(
    @Schema(example = "3") val id: Int? = null,
    @JsonProperty("alerta_id") @Schema(example = "7") val alertaId: Int,
    @JsonProperty("usuario_id") @Schema(example = "22") val usuarioId: Int,
    @Schema(example = "12.1400") val latitud: Double? = null,
    @Schema(example = "-86.2500") val longitud: Double? = null,
    @Schema(example = "La vi cerca del parque hace 10 minutos") val comentario: String? = null,
    @JsonProperty("imagen_url") val imagenUrl: String? = null,
    val fecha: String? = null
) {
    companion object {
        fun fromEntity(entity: Avistamiento) = AvistamientoDTO(
            id = entity.id,
            alertaId = entity.alertaId ?: 0,
            usuarioId = entity.usuarioId ?: 0,
            latitud = entity.latitud,
            longitud = entity.longitud,
            comentario = entity.comentario,
            imagenUrl = entity.imagenUrl,
            fecha = entity.fecha?.toString()
        )
    }

    fun toEntity(): Avistamiento = Avistamiento(
        alertaId = alertaId,
        usuarioId = usuarioId,
        latitud = latitud,
        longitud = longitud,
        comentario = comentario,
        imagenUrl = imagenUrl
    )
}
