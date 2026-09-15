package com.petcare.model

/*
 * Comentario de modulo PetCare:
 * Entidad de dominio. Representa una tabla o concepto principal usado por la API.
 */

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "expediente_medico")
data class ExpedienteMedico(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(name = "pets_id")
    var petsId: Int? = null,

    @Column(nullable = false)
    var tipo: String = "NOTA",

    @Column(nullable = false)
    var titulo: String = "",

    @Column(columnDefinition = "text")
    var descripcion: String? = null,

    @Column(nullable = false)
    var fecha: LocalDate = LocalDate.now(),

    @Column(name = "fecha_proxima")
    var fechaProxima: LocalDate? = null,

    @Column(name = "veterinario_nombre")
    var veterinarioNombre: String? = null,

    @Column(name = "veterinario_telefono")
    var veterinarioTelefono: String? = null,

    @Column(name = "imagen_carnet_url", columnDefinition = "text")
    var imagenCarnetUrl: String? = null,

    @Column(name = "fecha_creacion")
    var fechaCreacion: LocalDateTime? = null
) {
    @PrePersist
    fun prePersist() {
        if (fechaCreacion == null) fechaCreacion = LocalDateTime.now()
    }
}
