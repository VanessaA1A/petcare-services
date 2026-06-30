package com.petcare.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun petCareOpenApi(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("PetCare API")
                    .description("API para usuarios, mascotas, servicios, postulaciones y calificaciones de PetCare.")
                    .version("1.0.0")
            )
    }
}
