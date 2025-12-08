package com.vettrack.core.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun vetTrackOpenAPI(): OpenAPI =
        OpenAPI().info(
            Info()
                .title("VetTrack Core API")
                .description("Core API for VetTrack")
                .version("1.0.0")
        )
}