package com.example.automationgateway.dto;

import com.example.automationgateway.model.DocumentStatus;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class N8nActionRequestDTOTest {

    @Test
    void allArgsConstructorAndGettersSettersWork() {
        AiAnalysisResultDTO analysis = new AiAnalysisResultDTO(
                "RAG_ANSWER",
                "create_invoice_entry",
                Map.of("answer", "42")
        );

        N8nActionRequestDTO req = new N8nActionRequestDTO(
                "doc-1",
                DocumentStatus.COMPLETED,
                "create_invoice_entry",
                "RAG_ANSWER",
                analysis
        );

        assertEquals("doc-1", req.getDocumentId());
        assertEquals(DocumentStatus.COMPLETED, req.getStatus());
        assertEquals("create_invoice_entry", req.getActionType());
        assertEquals("RAG_ANSWER", req.getType());
        assertSame(analysis, req.getAnalysis());

        AiAnalysisResultDTO analysis2 = new AiAnalysisResultDTO(
                "ERROR",
                "notify_error",
                Map.of("message", "fail")
        );

        req.setDocumentId("doc-2");
        req.setStatus(DocumentStatus.FAILED);
        req.setActionType("notify_error");
        req.setType("ERROR");
        req.setAnalysis(analysis2);

        assertEquals("doc-2", req.getDocumentId());
        assertEquals(DocumentStatus.FAILED, req.getStatus());
        assertEquals("notify_error", req.getActionType());
        assertEquals("ERROR", req.getType());
        assertSame(analysis2, req.getAnalysis());
    }

    @Test
    void noArgsConstructorCreatesNullFields() {
        N8nActionRequestDTO req = new N8nActionRequestDTO();
        assertNull(req.getDocumentId());
        assertNull(req.getStatus());
        assertNull(req.getActionType());
        assertNull(req.getType());
        assertNull(req.getAnalysis());
    }

    @Test
    void equalsAndHashCodeWork() {
        AiAnalysisResultDTO analysis = new AiAnalysisResultDTO(
                "RAG_ANSWER",
                "create_invoice_entry",
                Map.of("answer", "42")
        );

        N8nActionRequestDTO a = new N8nActionRequestDTO(
                "doc-1",
                DocumentStatus.COMPLETED,
                "create_invoice_entry",
                "RAG_ANSWER",
                analysis
        );

        N8nActionRequestDTO b = new N8nActionRequestDTO(
                "doc-1",
                DocumentStatus.COMPLETED,
                "create_invoice_entry",
                "RAG_ANSWER",
                analysis
        );

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
