package com.example.automationgateway.service;

import com.example.automationgateway.dto.AiAnalysisResultDTO;
import com.example.automationgateway.dto.DocumentRequestDTO;
import com.example.automationgateway.dto.DocumentResponseDTO;
import com.example.automationgateway.dto.RagQueryResponseDTO;
import com.example.automationgateway.dto.RagRetrievedDocumentDTO;
import com.example.automationgateway.model.Document;
import com.example.automationgateway.model.DocumentStatus;
import com.example.automationgateway.repository.DocumentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private RagDirectService ragDirectService;

    @Mock
    private N8nService n8NService;

    private ObjectMapper objectMapper;

    private DocumentService documentService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        documentService = new DocumentService(
                documentRepository,
                ragDirectService,
                objectMapper,
                n8NService
        );
        // No default stubbing here; each test sets up only what it needs.
    }

    @Test
    void processDocumentSuccessPathSetsCompletedStatusAndRagAnswerAndCallsN8n() {
        DocumentRequestDTO request = new DocumentRequestDTO("some text");

        RagRetrievedDocumentDTO retrieved = new RagRetrievedDocumentDTO(
                "doc1", 0.95, "retrieved text", Map.of("pmcid", "PMC123")
        );
        RagQueryResponseDTO ragResponse = new RagQueryResponseDTO(
                "some text",
                "rag answer",
                List.of(retrieved)
        );

        when(ragDirectService.query("some text", 5)).thenReturn(ragResponse);

        // save(...) should return the same instance that is passed in
        when(documentRepository.save(any(Document.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DocumentResponseDTO response = documentService.processDocument(request);

        assertNotNull(response.getId());
        assertEquals(DocumentStatus.COMPLETED, response.getStatus());
        assertEquals("RAG_ANSWER", response.getType());

        AiAnalysisResultDTO analysis = response.getAnalysis();
        assertNotNull(analysis);
        assertEquals("RAG_ANSWER", analysis.getType());
        // new actionType for success path:
        assertEquals("create_invoice_entry", analysis.getActionType());
        assertEquals("some text", analysis.getFields().get("query"));
        assertEquals("rag answer", analysis.getFields().get("answer"));
        assertEquals("rag-agent-service", analysis.getFields().get("source"));

        // Check that the stored document has correct state and analysisJson
        ArgumentCaptor<Document> docCaptor = ArgumentCaptor.forClass(Document.class);
        verify(documentRepository, atLeastOnce()).save(docCaptor.capture());
        Document lastSaved = docCaptor.getValue();
        assertEquals(DocumentStatus.COMPLETED, lastSaved.getStatus());
        assertEquals("RAG_ANSWER", lastSaved.getType());
        assertNotNull(lastSaved.getAnalysisJson());
        assertDoesNotThrow(() ->
                objectMapper.readValue(lastSaved.getAnalysisJson(), AiAnalysisResultDTO.class)
        );

        // n8n must be called once with the final document + analysis
        ArgumentCaptor<AiAnalysisResultDTO> analysisCaptor = ArgumentCaptor.forClass(AiAnalysisResultDTO.class);
        verify(n8NService, times(1)).sendAiAction(eq(lastSaved), analysisCaptor.capture());
        AiAnalysisResultDTO sentToN8n = analysisCaptor.getValue();
        assertEquals("create_invoice_entry", sentToN8n.getActionType());
    }

    @Test
    void processDocumentFailurePathSetsFailedStatusAndErrorTypeAndCallsN8n() {
        DocumentRequestDTO request = new DocumentRequestDTO("some text");

        RuntimeException cause = new RuntimeException("backend down");
        when(ragDirectService.query("some text", 5)).thenThrow(cause);

        when(documentRepository.save(any(Document.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DocumentResponseDTO response = documentService.processDocument(request);

        assertEquals(DocumentStatus.FAILED, response.getStatus());
        assertEquals("ERROR", response.getType());

        AiAnalysisResultDTO analysis = response.getAnalysis();
        assertNotNull(analysis);
        assertEquals("ERROR", analysis.getType());
        // new actionType for error path:
        assertEquals("notify_error", analysis.getActionType());
        assertEquals("RuntimeException", analysis.getFields().get("errorType"));
        assertTrue(((String) analysis.getFields().get("message"))
                .contains("backend down"));

        ArgumentCaptor<Document> docCaptor = ArgumentCaptor.forClass(Document.class);
        verify(documentRepository, atLeastOnce()).save(docCaptor.capture());
        Document lastSaved = docCaptor.getValue();
        assertEquals(DocumentStatus.FAILED, lastSaved.getStatus());
        assertEquals("ERROR", lastSaved.getType());
        assertNotNull(lastSaved.getAnalysisJson());

        // n8n must also be called in the error case
        ArgumentCaptor<AiAnalysisResultDTO> analysisCaptor = ArgumentCaptor.forClass(AiAnalysisResultDTO.class);
        verify(n8NService, times(1)).sendAiAction(eq(lastSaved), analysisCaptor.capture());
        AiAnalysisResultDTO sentToN8n = analysisCaptor.getValue();
        assertEquals("notify_error", sentToN8n.getActionType());
    }

    @Test
    void getDocumentReturnsMappedResponseWithAnalysis() throws JsonProcessingException {
        Document doc = new Document();
        doc.setId("doc-1");
        doc.setRawText("raw");
        doc.setStatus(DocumentStatus.COMPLETED);
        doc.setType("RAG_ANSWER");
        doc.setCreatedAt(Instant.parse("2025-01-01T10:00:00Z"));
        doc.setUpdatedAt(Instant.parse("2025-01-01T10:05:00Z"));

        AiAnalysisResultDTO storedAnalysis = new AiAnalysisResultDTO(
                "RAG_ANSWER",
                "create_invoice_entry",
                Map.of("answer", "42")
        );
        String json = objectMapper.writeValueAsString(storedAnalysis);
        doc.setAnalysisJson(json);

        when(documentRepository.findById("doc-1")).thenReturn(Optional.of(doc));

        Optional<DocumentResponseDTO> opt = documentService.getDocument("doc-1");

        assertTrue(opt.isPresent());
        DocumentResponseDTO resp = opt.get();
        assertEquals("doc-1", resp.getId());
        assertEquals(DocumentStatus.COMPLETED, resp.getStatus());
        assertEquals("RAG_ANSWER", resp.getType());
        assertEquals(Instant.parse("2025-01-01T10:00:00Z"), resp.getCreatedAt());

        AiAnalysisResultDTO analysis = resp.getAnalysis();
        assertNotNull(analysis);
        assertEquals("RAG_ANSWER", analysis.getType());
        assertEquals("create_invoice_entry", analysis.getActionType());
        assertEquals("42", analysis.getFields().get("answer"));

        // getDocument() should not interact with n8n
        verifyNoInteractions(n8NService);
    }

    @Test
    void getDocumentWithInvalidAnalysisJsonReturnsResponseWithoutAnalysis() {
        Document doc = new Document();
        doc.setId("doc-2");
        doc.setRawText("raw");
        doc.setStatus(DocumentStatus.COMPLETED);
        doc.setType("RAG_ANSWER");
        doc.setCreatedAt(Instant.now());
        doc.setUpdatedAt(Instant.now());
        doc.setAnalysisJson("{not-valid-json"); // invalid JSON

        when(documentRepository.findById("doc-2")).thenReturn(Optional.of(doc));

        Optional<DocumentResponseDTO> opt = documentService.getDocument("doc-2");

        assertTrue(opt.isPresent());
        DocumentResponseDTO resp = opt.get();
        assertEquals("doc-2", resp.getId());
        assertEquals(DocumentStatus.COMPLETED, resp.getStatus());
        assertEquals("RAG_ANSWER", resp.getType());
        assertNull(resp.getAnalysis()); // deserialization failed → null

        // still no interaction with n8n
        verifyNoInteractions(n8NService);
    }
}
