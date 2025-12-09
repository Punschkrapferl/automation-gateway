package com.example.automationgateway.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenApiConfigTest {

    @Test
    void automationGatewayOpenAPIBuildsExpectedModel() {
        OpenApiConfig config = new OpenApiConfig();

        OpenAPI openAPI = config.automationGatewayOpenAPI();
        assertNotNull(openAPI, "OpenAPI instance must not be null");

        // Info block
        assertNotNull(openAPI.getInfo(), "Info must not be null");
        assertEquals("AI Automation Gateway API", openAPI.getInfo().getTitle());
        assertEquals("v1.0", openAPI.getInfo().getVersion());
        assertNotNull(openAPI.getInfo().getDescription());
        assertTrue(
                openAPI.getInfo().getDescription().toLowerCase().contains("document analysis"),
                "Description should mention document analysis"
        );

        // External docs
        ExternalDocumentation externalDocs = openAPI.getExternalDocs();
        assertNotNull(externalDocs, "ExternalDocumentation must not be null");
        assertEquals("https://example.com/your-repo", externalDocs.getUrl());
        assertNotNull(externalDocs.getDescription());
    }
}
