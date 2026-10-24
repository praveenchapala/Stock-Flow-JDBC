
package com.stockflow.api;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "StockFlow Inventory Management API",
        version = "1.0.0",
        description = "REST API for managing inventory products using Spring Boot and MySQL."
    )
)
public class OpenApiConfig {
}
