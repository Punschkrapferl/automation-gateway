package com.example.automationgateway.service;

import com.example.automationgateway.dto.AiAnalysisResultDTO;
import com.example.automationgateway.dto.N8nActionRequestDTO;
import com.example.automationgateway.model.Document;
import com.example.automationgateway.model.DocumentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class N8nClientTest {

    @Mock
    private RestTemplate restTemplate;

    private N8nService n8NService;

    private final String baseUrl = "http://n8n:5678";
    private final String webhookPath = "/webhook/ai-action";

    @BeforeEach
    void setUp() {
        n8NService = new N8nService(restTemplate, baseUrl, webhookPath);
    }

    @Test
    void sendAiActionSkipsWhenAnalysisIsNull() {
        Document doc = new Document();
        doc.setId("doc-1");
        doc.setStatus(DocumentStatus.COMPLETED);

        n8NService.sendAiAction(doc, null);

        verifyNoInteractions(restTemplate);
    }

    @Test
    void sendAiActionSkipsWhenActionTypeIsNull() {
        Document doc = new Document();
        doc.setId("doc-1");
        doc.setStatus(DocumentStatus.COMPLETED);

        AiAnalysisResultDTO analysis = new AiAnalysisResultDTO(
                "RAG_ANSWER",
                null,
                Map.of("answer", "42")
        );

        n8NService.sendAiAction(doc, analysis);

        verifyNoInteractions(restTemplate);
    }

    @Test
    void sendAiActionSkipsWhenActionTypeIsBlank() {
        Document doc = new Document();
        doc.setId("doc-1");
        doc.setStatus(DocumentStatus.COMPLETED);

        AiAnalysisResultDTO analysis = new AiAnalysisResultDTO(
                "RAG_ANSWER",
                "   ",
                Map.of("answer", "42")
        );

        n8NService.sendAiAction(doc, analysis);

        verifyNoInteractions(restTemplate);
    }

    @Test
    void sendAiActionPostsPayloadToWebhook() {
        Document doc = new Document();
        doc.setId("doc-1");
        doc.setStatus(DocumentStatus.COMPLETED);

        AiAnalysisResultDTO analysis = new AiAnalysisResultDTO(
                "RAG_ANSWER",
                "create_invoice_entry",
                Map.of("answer", "42")
        );

        ResponseEntity<String> ok = new ResponseEntity<>("OK", HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(ok);

        n8NService.sendAiAction(doc, analysis);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        @SuppressWarnings("rawtypes")
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        verify(restTemplate, times(1)).exchange(
                urlCaptor.capture(),
                eq(HttpMethod.POST),
                entityCaptor.capture(),
                eq(String.class)
        );

        assertEquals(baseUrl + webhookPath, urlCaptor.getValue());

        Object body = entityCaptor.getValue().getBody();
        assertNotNull(body);
        assertTrue(body instanceof N8nActionRequestDTO);

        N8nActionRequestDTO payload = (N8nActionRequestDTO) body;
        assertEquals("doc-1", payload.getDocumentId());
        assertEquals(DocumentStatus.COMPLETED, payload.getStatus());
        assertEquals("create_invoice_entry", payload.getActionType());
        assertEquals("RAG_ANSWER", payload.getType());
        assertSame(analysis, payload.getAnalysis());
    }

    @Test
    void sendAiActionDefaultsStatusToCompletedWhenDocumentStatusIsNull() {
        Document doc = new Document();
        doc.setId("doc-1");
        doc.setStatus(null);

        AiAnalysisResultDTO analysis = new AiAnalysisResultDTO(
                "RAG_ANSWER",
                "create_support_ticket",
                Map.of("ticket", "T-1")
        );

        ResponseEntity<String> ok = new ResponseEntity<>("OK", HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(ok);

        n8NService.sendAiAction(doc, analysis);

        @SuppressWarnings("rawtypes")
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        verify(restTemplate, times(1)).exchange(
                eq(baseUrl + webhookPath),
                eq(HttpMethod.POST),
                entityCaptor.capture(),
                eq(String.class)
        );

        N8nActionRequestDTO payload = (N8nActionRequestDTO) entityCaptor.getValue().getBody();
        assertNotNull(payload);
        assertEquals(DocumentStatus.COMPLETED, payload.getStatus());
    }

    @Test
    void sendAiActionSwallowsExceptionsFromRestTemplate() {
        Document doc = new Document();
        doc.setId("doc-1");
        doc.setStatus(DocumentStatus.COMPLETED);

        AiAnalysisResultDTO analysis = new AiAnalysisResultDTO(
                "RAG_ANSWER",
                "create_invoice_entry",
                Map.of("answer", "42")
        );

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(new RuntimeException("connection refused"));

        assertDoesNotThrow(() -> n8NService.sendAiAction(doc, analysis));

        verify(restTemplate, times(1)).exchange(
                eq(baseUrl + webhookPath),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        );
    }
}
