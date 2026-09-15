package com.petcare

/*
 * Comentario de modulo PetCare:
 * Archivo del proyecto PetCare. Mantiene una parte especifica de la app y debe conservarse simple de seguir.
 */

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class PetcareApplication

fun main(args: Array<String>) {
    runApplication<PetcareApplication>(*args)
}
