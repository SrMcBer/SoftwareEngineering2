package com.vettrack.core.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class AuthRestClientConfig {

    @Bean
    fun authRestClient(
        @Value("http://host.docker.internal:8000") baseUrl: String
    ): RestClient {
        return RestClient.builder()
            .baseUrl(baseUrl)
            .build()
    }
}