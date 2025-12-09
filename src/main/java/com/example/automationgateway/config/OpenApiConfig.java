package com.example.automationgateway.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger configuration for the Automation Gateway application.
 * <p>
 * This configuration class registers a single {@link OpenAPI} bean which is
 * picked up by springdoc-openapi to generate the OpenAPI 3 specification and
 * render the Swagger UI.
 * </p>
 */
@Configuration
public class OpenApiConfig {

    /**
     * Creates and configures the OpenAPI definition for the Automation Gateway API.
     * <p>
     * The returned {@link OpenAPI} object defines:
     * </p>
     * <ul>
     *   <li><b>Title</b>: human-readable name of the API</li>
     *   <li><b>Description</b>: short explanation of what the API does</li>
     *   <li><b>Version</b>: API version identifier</li>
     *   <li><b>External documentation</b>: link to the project repository or
     *       additional docs</li>
     * </ul>
     *
     * @return configured {@link OpenAPI} instance used by springdoc-openapi
     */
    @Bean
    public OpenAPI automationGatewayOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AI Automation Gateway API")
                        .description("REST API for document analysis and automation via a RAG-backed service")
                        .version("v1.0"))
                .externalDocs(new ExternalDocumentation()
                        .description("Project repository and additional documentation")
                        .url("https://example.com/your-repo"));
    }
}
