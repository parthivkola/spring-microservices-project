package com.microservices.notification.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * OpenApiConfig — Swagger UI at http://localhost:8084/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI notificationOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Notification Service")
                        .description("Kafka consumer that stores and exposes order notifications")
                        .version("1.0"));
    }
}
