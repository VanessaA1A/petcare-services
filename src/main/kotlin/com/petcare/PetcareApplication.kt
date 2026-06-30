package com.petcare

/*
 * Comentario de modulo PetCare:
 * Archivo del proyecto PetCare. Mantiene una parte especifica de la app y debe conservarse simple de seguir.
 */

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PetcareApplication

fun main(args: Array<String>) {
    runApplication<PetcareApplication>(*args)
}
