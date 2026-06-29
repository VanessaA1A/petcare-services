package com.petcare.service

class CancellationNotAllowedException(
    message: String = "Solo puedes cancelar un servicio hasta 3 horas antes de que empiece."
) : RuntimeException(message)
