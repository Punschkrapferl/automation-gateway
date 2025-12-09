package com.example.automationgateway.dto;

import com.example.automationgateway.model.DocumentStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DocumentResponseTest {

    @Test
    void allArgsConstructorAndGettersWork() {
        AiAnalysisResult analysis = new AiAnalysisResult(
                "RAG_ANSWER",
                "rag-agent-service",
                Map.of("answer", "42")
        );
        Instant created = Instant.parse("2025-01-01T10:00:00Z");
        Instant updated = Instant.parse("2025-01-01T10:05:00Z");

        DocumentResponse resp = new DocumentResponse(
                "doc-1",
                DocumentStatus.COMPLETED,
                "RAG_ANSWER",
                analysis,
                created,
                updated
        );

        assertEquals("doc-1", resp.getId());
        assertEquals(DocumentStatus.COMPLETED, resp.getStatus());
        assertEquals("RAG_ANSWER", resp.getType());
        assertSame(analysis, resp.getAnalysis());
        assertEquals(created, resp.getCreatedAt());
        assertEquals(updated, resp.getUpdatedAt());
    }

    @Test
    void noArgsConstructorAndSettersWork() {
        AiAnalysisResult analysis = new AiAnalysisResult(
                "ERROR",
                "rag-agent-service",
                Map.of("errorType", "RuntimeException")
        );
        Instant created = Instant.now();
        Instant updated = created.plusSeconds(5);

        DocumentResponse resp = new DocumentResponse();
        resp.setId("doc-2");
        resp.setStatus(DocumentStatus.FAILED);
        resp.setType("ERROR");
        resp.setAnalysis(analysis);
        resp.setCreatedAt(created);
        resp.setUpdatedAt(updated);

        assertEquals("doc-2", resp.getId());
        assertEquals(DocumentStatus.FAILED, resp.getStatus());
        assertEquals("ERROR", resp.getType());
        assertSame(analysis, resp.getAnalysis());
        assertEquals(created, resp.getCreatedAt());
        assertEquals(updated, resp.getUpdatedAt());
    }

    @Test
    void equalsAndHashCodeConsiderAllFields() {
        AiAnalysisResult analysis1 = new AiAnalysisResult(
                "T",
                "A",
                Map.of("k", "v")
        );
        AiAnalysisResult analysis2 = new AiAnalysisResult(
                "T",
                "A",
                Map.of("k", "v")
        );
        Instant created = Instant.parse("2025-01-01T10:00:00Z");
        Instant updated = Instant.parse("2025-01-01T10:05:00Z");

        DocumentResponse r1 = new DocumentResponse(
                "id-1", DocumentStatus.PROCESSING, "T", analysis1, created, updated
        );
        DocumentResponse r2 = new DocumentResponse(
                "id-1", DocumentStatus.PROCESSING, "T", analysis2, created, updated
        );

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());

        DocumentResponse r3 = new DocumentResponse(
                "id-2", DocumentStatus.PROCESSING, "T", analysis1, created, updated
        );
        assertNotEquals(r1, r3);
    }

    @Test
    void toStringContainsKeyFields() {
        AiAnalysisResult analysis = new AiAnalysisResult(
                "T",
                "A",
                Map.of("k", "v")
        );
        Instant created = Instant.parse("2025-01-01T10:00:00Z");
        Instant updated = Instant.parse("2025-01-01T10:05:00Z");

        DocumentResponse resp = new DocumentResponse(
                "doc-x",
                DocumentStatus.COMPLETED,
                "T",
                analysis,
                created,
                updated
        );

        String s = resp.toString();
        assertTrue(s.contains("doc-x"));
        assertTrue(s.contains("COMPLETED"));
        assertTrue(s.contains("T"));
    }
}
