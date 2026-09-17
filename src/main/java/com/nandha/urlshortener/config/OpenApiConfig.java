package com.nandha.urlshortener.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures OpenAPI metadata.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Defines API information displayed in Swagger UI.
     */
    @Bean
    public OpenAPI urlShortenerOpenApi() {

        return new OpenAPI()
                .info(
                        new Info()
                                .title("URL Shortener API")
                                .version("1.0.0")
                                .description(
                                        "A URL shortening service built with Spring Boot, MySQL and Redis."
                                )
                                .contact(
                                        new Contact()
                                                .name("Nandha Kumar")
                                                .email("nandhak131106@example.com")
                                )
                );
    }
}