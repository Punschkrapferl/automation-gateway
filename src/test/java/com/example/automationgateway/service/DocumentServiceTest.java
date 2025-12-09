package com.example.automationgateway.service;

import com.example.automationgateway.dto.AiAnalysisResult;
import com.example.automationgateway.dto.DocumentRequest;
import com.example.automationgateway.dto.DocumentResponse;
import com.example.automationgateway.dto.RagQueryResponse;
import com.example.automationgateway.dto.RagRetrievedDocument;
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

    private ObjectMapper objectMapper;

    private DocumentService documentService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        documentService = new DocumentService(documentRepository, ragDirectService, objectMapper);
        // IMPORTANT: no stubbing here → avoids UnnecessaryStubbing for tests that only call getDocument()
    }

    @Test
    void processDocumentSuccessPathSetsCompletedStatusAndRagAnswer() {
        DocumentRequest request = new DocumentRequest("some text");

        RagRetrievedDocument retrieved = new RagRetrievedDocument(
                "doc1", 0.95, "retrieved text", Map.of("pmcid", "PMC123")
        );
        RagQueryResponse ragResponse = new RagQueryResponse(
                "some text",
                "rag answer",
                List.of(retrieved)
        );

        when(ragDirectService.query("some text", 5)).thenReturn(ragResponse);

        // For this test we do need save(...) to return the same instance
        when(documentRepository.save(any(Document.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DocumentResponse response = documentService.processDocument(request);

        assertNotNull(response.getId());
        assertEquals(DocumentStatus.COMPLETED, response.getStatus());
        assertEquals("RAG_ANSWER", response.getType());

        AiAnalysisResult analysis = response.getAnalysis();
        assertNotNull(analysis);
        assertEquals("RAG_ANSWER", analysis.getType());
        assertEquals("rag-agent-service", analysis.getActionType());
        assertEquals("some text", analysis.getFields().get("query"));
        assertEquals("rag answer", analysis.getFields().get("answer"));

        // Check that the stored document has correct state and analysisJson
        ArgumentCaptor<Document> captor = ArgumentCaptor.forClass(Document.class);
        verify(documentRepository, atLeastOnce()).save(captor.capture());
        Document lastSaved = captor.getValue();
        assertEquals(DocumentStatus.COMPLETED, lastSaved.getStatus());
        assertEquals("RAG_ANSWER", lastSaved.getType());
        assertNotNull(lastSaved.getAnalysisJson());
        assertDoesNotThrow(() ->
                objectMapper.readValue(lastSaved.getAnalysisJson(), AiAnalysisResult.class)
        );
    }

    @Test
    void processDocumentFailurePathSetsFailedStatusAndErrorType() {
        DocumentRequest request = new DocumentRequest("some text");

        RuntimeException cause = new RuntimeException("backend down");
        when(ragDirectService.query("some text", 5)).thenThrow(cause);

        // Again, only this test needs save(...) stubbing
        when(documentRepository.save(any(Document.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DocumentResponse response = documentService.processDocument(request);

        assertEquals(DocumentStatus.FAILED, response.getStatus());
        assertEquals("ERROR", response.getType());

        AiAnalysisResult analysis = response.getAnalysis();
        assertNotNull(analysis);
        assertEquals("ERROR", analysis.getType());
        assertEquals("rag-agent-service", analysis.getActionType());
        assertEquals("RuntimeException", analysis.getFields().get("errorType"));
        assertTrue(((String) analysis.getFields().get("message"))
                .contains("backend down"));

        ArgumentCaptor<Document> captor = ArgumentCaptor.forClass(Document.class);
        verify(documentRepository, atLeastOnce()).save(captor.capture());
        Document lastSaved = captor.getValue();
        assertEquals(DocumentStatus.FAILED, lastSaved.getStatus());
        assertEquals("ERROR", lastSaved.getType());
        assertNotNull(lastSaved.getAnalysisJson());
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

        AiAnalysisResult storedAnalysis = new AiAnalysisResult(
                "RAG_ANSWER",
                "rag-agent-service",
                Map.of("answer", "42")
        );
        String json = objectMapper.writeValueAsString(storedAnalysis);
        doc.setAnalysisJson(json);

        when(documentRepository.findById("doc-1")).thenReturn(Optional.of(doc));

        Optional<DocumentResponse> opt = documentService.getDocument("doc-1");

        assertTrue(opt.isPresent());
        DocumentResponse resp = opt.get();
        assertEquals("doc-1", resp.getId());
        assertEquals(DocumentStatus.COMPLETED, resp.getStatus());
        assertEquals("RAG_ANSWER", resp.getType());
        assertEquals(Instant.parse("2025-01-01T10:00:00Z"), resp.getCreatedAt());

        AiAnalysisResult analysis = resp.getAnalysis();
        assertNotNull(analysis);
        assertEquals("RAG_ANSWER", analysis.getType());
        assertEquals("rag-agent-service", analysis.getActionType());
        assertEquals("42", analysis.getFields().get("answer"));
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
        doc.setAnalysisJson("{not-valid-json"); // invalid

        when(documentRepository.findById("doc-2")).thenReturn(Optional.of(doc));

        Optional<DocumentResponse> opt = documentService.getDocument("doc-2");

        assertTrue(opt.isPresent());
        DocumentResponse resp = opt.get();
        assertEquals("doc-2", resp.getId());
        assertEquals(DocumentStatus.COMPLETED, resp.getStatus());
        assertEquals("RAG_ANSWER", resp.getType());
        assertNull(resp.getAnalysis()); // deserialization failed → null
    }
}
