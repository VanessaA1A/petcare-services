package com.petcare.service

/*
 * Comentario de modulo PetCare:
 * Servicio de negocio. Contiene reglas de PetCare que no deben vivir directamente en los controladores.
 */

class CancellationNotAllowedException(
    message: String = "Solo puedes cancelar un servicio hasta 3 horas antes de que empiece."
) : RuntimeException(message)
