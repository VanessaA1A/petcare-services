package com.petcare.dto

/*
 * Comentario de modulo PetCare:
 * Objeto de transferencia. Define la forma de los datos que entran o salen por la API.
 */

import com.fasterxml.jackson.annotation.JsonProperty
import com.petcare.model.ExpedienteMedico
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class ExpedienteMedicoDTO(
    @Schema(example = "9") val id: Int? = null,
    @JsonProperty("pets_id") @Schema(example = "4") val petsId: Int,
    @Schema(example = "VACUNA", description = "VACUNA, DESPARASITACION, ALERGIA, MEDICAMENTO, CIRUGIA, PESO o NOTA") val tipo: String,
    @Schema(example = "Vacuna antirrábica") val titulo: String,
    @Schema(example = "Dosis anual aplicada en la clínica del barrio") val descripcion: String? = null,
    @Schema(example = "2026-09-15") val fecha: String,
    @JsonProperty("fecha_proxima") @Schema(example = "2027-09-15") val fechaProxima: String? = null,
    @JsonProperty("veterinario_nombre") @Schema(example = "Dra. López") val veterinarioNombre: String? = null,
    @JsonProperty("veterinario_telefono") @Schema(example = "+505 8888 3333") val veterinarioTelefono: String? = null,
    @JsonProperty("imagen_carnet_url") @Schema(example = "/api/pets/expediente/carnet_4_123.jpg") val imagenCarnetUrl: String? = null,
    @JsonProperty("fecha_creacion") @Schema(example = "2026-09-15T10:00:00") val fechaCreacion: String? = null
) {
    companion object {
        val TIPOS_VALIDOS = setOf("VACUNA", "DESPARASITACION", "ALERGIA", "MEDICAMENTO", "CIRUGIA", "PESO", "NOTA")

        fun fromEntity(entity: ExpedienteMedico) = ExpedienteMedicoDTO(
            id = entity.id,
            petsId = entity.petsId ?: 0,
            tipo = entity.tipo,
            titulo = entity.titulo,
            descripcion = entity.descripcion,
            fecha = entity.fecha.toString(),
            fechaProxima = entity.fechaProxima?.toString(),
            veterinarioNombre = entity.veterinarioNombre,
            veterinarioTelefono = entity.veterinarioTelefono,
            imagenCarnetUrl = entity.imagenCarnetUrl,
            fechaCreacion = entity.fechaCreacion?.toString()
        )
    }

    fun toEntity(existing: ExpedienteMedico? = null): ExpedienteMedico {
        val entity = existing ?: ExpedienteMedico()
        entity.petsId = petsId
        entity.tipo = tipo
        entity.titulo = titulo
        entity.descripcion = descripcion
        entity.fecha = runCatching { LocalDate.parse(fecha) }.getOrDefault(LocalDate.now())
        entity.fechaProxima = fechaProxima?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        entity.veterinarioNombre = veterinarioNombre
        entity.veterinarioTelefono = veterinarioTelefono
        return entity
    }
}
