package com.petcare;

/*
 * Comentario de modulo PetCare:
 * Clase Java del proyecto PetCare. Mantiene compatibilidad con partes del backend que aun no estan en Kotlin.
 */

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PetcareApplication {
    public static void main(String[] args) {
        SpringApplication.run(PetcareApplication.class, args);
    }
}
