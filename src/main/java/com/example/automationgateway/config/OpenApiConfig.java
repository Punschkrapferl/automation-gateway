package com.example.automationgateway.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI automationGatewayOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AI Automation Gateway API")
                        .description("REST API for document analysis and automation")
                        .version("v1.0"))
                .externalDocs(new ExternalDocumentation()
                        .description("Project Repository")
                        .url("https://example.com/your-repo"));
    }
}
