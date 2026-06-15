package com.petcare

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PetcareApplication

fun main(args: Array<String>) {
    runApplication<PetcareApplication>(*args)
}
