package com.petcare.util

/*
 * Comentario de modulo PetCare:
 * Utilidad compartida. Evita repetir reglas pequenas en varias partes del proyecto.
 */

import java.security.MessageDigest

object HashUtil {
    fun md5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }
}
