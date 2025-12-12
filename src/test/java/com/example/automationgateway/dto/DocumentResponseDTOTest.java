package com.example.automationgateway.dto;

import com.example.automationgateway.model.DocumentStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DocumentResponseDTOTest {

    @Test
    void allArgsConstructorAndGettersWork() {
        AiAnalysisResultDTO analysis = new AiAnalysisResultDTO(
                "RAG_ANSWER",
                "rag-agent-service",
                Map.of("answer", "42")
        );
        Instant created = Instant.parse("2025-01-01T10:00:00Z");
        Instant updated = Instant.parse("2025-01-01T10:05:00Z");

        DocumentResponseDTO resp = new DocumentResponseDTO(
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
        AiAnalysisResultDTO analysis = new AiAnalysisResultDTO(
                "ERROR",
                "rag-agent-service",
                Map.of("errorType", "RuntimeException")
        );
        Instant created = Instant.now();
        Instant updated = created.plusSeconds(5);

        DocumentResponseDTO resp = new DocumentResponseDTO();
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
        AiAnalysisResultDTO analysis1 = new AiAnalysisResultDTO(
                "T",
                "A",
                Map.of("k", "v")
        );
        AiAnalysisResultDTO analysis2 = new AiAnalysisResultDTO(
                "T",
                "A",
                Map.of("k", "v")
        );
        Instant created = Instant.parse("2025-01-01T10:00:00Z");
        Instant updated = Instant.parse("2025-01-01T10:05:00Z");

        DocumentResponseDTO r1 = new DocumentResponseDTO(
                "id-1", DocumentStatus.PROCESSING, "T", analysis1, created, updated
        );
        DocumentResponseDTO r2 = new DocumentResponseDTO(
                "id-1", DocumentStatus.PROCESSING, "T", analysis2, created, updated
        );

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());

        DocumentResponseDTO r3 = new DocumentResponseDTO(
                "id-2", DocumentStatus.PROCESSING, "T", analysis1, created, updated
        );
        assertNotEquals(r1, r3);
    }

    @Test
    void toStringContainsKeyFields() {
        AiAnalysisResultDTO analysis = new AiAnalysisResultDTO(
                "T",
                "A",
                Map.of("k", "v")
        );
        Instant created = Instant.parse("2025-01-01T10:00:00Z");
        Instant updated = Instant.parse("2025-01-01T10:05:00Z");

        DocumentResponseDTO resp = new DocumentResponseDTO(
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
