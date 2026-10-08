package com.microservices.inventory.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * OpenApiConfig — springdoc OpenAPI metadata for Swagger UI.
 *
 * Visit: http://localhost:8085/swagger-ui.html
 * springdoc scans @RestController methods and builds the OpenAPI 3 document.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI inventoryOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Inventory Service")
                        .description("Redis-backed inventory consumer for OrderCreatedEvent (Kafka)")
                        .version("1.0"));
    }
}
