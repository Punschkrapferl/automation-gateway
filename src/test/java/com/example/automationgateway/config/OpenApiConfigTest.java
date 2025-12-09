package com.example.automationgateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.ExternalDocumentation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenApiConfigTest {

    @Test
    void automationGatewayOpenAPIBuildsExpectedModel() {
        OpenApiConfig config = new OpenApiConfig();

        OpenAPI openAPI = config.automationGatewayOpenAPI();
        assertNotNull(openAPI);

        Info info = openAPI.getInfo();
        assertNotNull(info);
        assertEquals("AI Automation Gateway API", info.getTitle());
        assertEquals("REST API for document analysis and automation", info.getDescription());
        assertEquals("v1.0", info.getVersion());

        ExternalDocumentation externalDocs = openAPI.getExternalDocs();
        assertNotNull(externalDocs);
        assertEquals("Project Repository", externalDocs.getDescription());
        assertEquals("https://example.com/your-repo", externalDocs.getUrl());
    }
}
