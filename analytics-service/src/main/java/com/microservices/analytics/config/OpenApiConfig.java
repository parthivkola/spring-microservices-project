package com.microservices.analytics.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * OpenApiConfig — Swagger UI at http://localhost:8086/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI analyticsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Analytics Service")
                        .description("Kafka consumer that aggregates order analytics")
                        .version("1.0"));
    }
}
