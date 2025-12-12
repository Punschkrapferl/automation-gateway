package com.example.automationgateway.controller;

import com.example.automationgateway.dto.AiAnalysisResultDTO;
import com.example.automationgateway.dto.DocumentRequestDTO;
import com.example.automationgateway.dto.DocumentResponseDTO;
import com.example.automationgateway.model.DocumentStatus;
import com.example.automationgateway.service.DocumentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DocumentController.class)
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // This is the mock bean, provided by the inner TestConfig below
    @Autowired
    private DocumentService documentService;

    @TestConfiguration
    static class TestConfig {

        @Bean
        public DocumentService documentService() {
            // Mockito mock that Spring will inject wherever DocumentService is required
            return Mockito.mock(DocumentService.class);
        }
    }

    @Test
    void createDocumentReturnsOkAndResponseBody() throws Exception {
        DocumentRequestDTO request = new DocumentRequestDTO("hello AI");

        AiAnalysisResultDTO analysis = new AiAnalysisResultDTO(
                "invoice",
                "create_ticket",
                Map.of("customer", "John Doe", "amount", 123.45)
        );

        DocumentResponseDTO response = new DocumentResponseDTO(
                "doc-123",
                DocumentStatus.COMPLETED,
                "invoice",
                analysis,
                Instant.parse("2025-01-01T10:00:00Z"),
                Instant.parse("2025-01-01T10:05:00Z")
        );

        Mockito.when(documentService.processDocument(any(DocumentRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("doc-123")))
                .andExpect(jsonPath("$.status", is("COMPLETED")))
                .andExpect(jsonPath("$.type", is("invoice")))
                .andExpect(jsonPath("$.analysis.type", is("invoice")))
                .andExpect(jsonPath("$.analysis.actionType", is("create_ticket")))
                .andExpect(jsonPath("$.analysis.fields.customer", is("John Doe")));
    }

    @Test
    void createDocumentWithEmptyTextReturnsBadRequest() throws Exception {
        DocumentRequestDTO invalidRequest = new DocumentRequestDTO(""); // violates @NotBlank

        mockMvc.perform(post("/api/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getDocumentFoundReturnsOk() throws Exception {
        AiAnalysisResultDTO analysis = new AiAnalysisResultDTO(
                "email",
                "reply",
                Map.of("subject", "Test")
        );

        DocumentResponseDTO response = new DocumentResponseDTO(
                "doc-456",
                DocumentStatus.COMPLETED,
                "email",
                analysis,
                Instant.parse("2025-01-01T10:00:00Z"),
                Instant.parse("2025-01-01T10:05:00Z")
        );

        Mockito.when(documentService.getDocument(eq("doc-456")))
                .thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/documents/{id}", "doc-456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("doc-456")))
                .andExpect(jsonPath("$.status", is("COMPLETED")))
                .andExpect(jsonPath("$.type", is("email")))
                .andExpect(jsonPath("$.analysis.type", is("email")));
    }

    @Test
    void getDocumentNotFoundReturns404() throws Exception {
        Mockito.when(documentService.getDocument(eq("unknown-id")))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/documents/{id}", "unknown-id"))
                .andExpect(status().isNotFound());
    }
}
