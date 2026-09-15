package com.petcare.util

/*
 * Comentario de modulo PetCare:
 * Utilidad compartida. Evita repetir reglas pequenas en varias partes del proyecto.
 */

/**
 * Restriccion de negocio: PetCare no permite la venta, comercializacion ni trafico de perros
 * (ni de ninguna otra especie) en la plataforma - solo servicios de cuidado. Se revisan los
 * textos libres que un usuario puede publicar (titulo/descripcion de una solicitud u oferta).
 */
object EticaUtil {
    private val PALABRAS_PROHIBIDAS = listOf(
        "venta", "vender", "se vende", "comprar perro", "precio de venta", "en venta"
    )

    fun contieneTextoDeVenta(vararg textos: String?): Boolean {
        return textos.filterNotNull().any { texto ->
            val normalizado = texto.lowercase()
            PALABRAS_PROHIBIDAS.any { normalizado.contains(it) }
        }
    }

    const val MENSAJE_RECHAZO = "No se permite la venta de animales en PetCare."

    const val TEXTO_REGLA_ETICA = """
        No se permite la venta de animales

        PetCare no permite la venta, comercializacion ni trafico de perros ni de ninguna otra
        especie animal en la plataforma. Esta decision se basa en:
        - El bienestar animal y la prevencion del maltrato.
        - La lucha contra el trafico ilegal de especies.
        - Estudios de organismos como la UICN y TRAFFIC sobre el comercio ilegal de fauna en
          Centroamerica.

        Cualquier usuario que intente usar la plataforma para vender animales sera bloqueado y
        reportado a las autoridades correspondientes.
    """
}
