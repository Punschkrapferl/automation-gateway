package com.example.automationgateway.dto;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AiAnalysisResultTest {

    @Test
    void allArgsConstructorAndGettersWork() {
        Map<String, Object> fields = Map.of("key", "value");

        AiAnalysisResult result = new AiAnalysisResult(
                "RAG_ANSWER",
                "rag-agent-service",
                fields
        );

        assertEquals("RAG_ANSWER", result.getType());
        assertEquals("rag-agent-service", result.getActionType());
        assertEquals(fields, result.getFields());
    }

    @Test
    void noArgsConstructorAndSettersWork() {
        AiAnalysisResult result = new AiAnalysisResult();

        result.setType("ERROR");
        result.setActionType("rag-agent-service");
        result.setFields(Map.of("errorType", "RuntimeException"));

        assertEquals("ERROR", result.getType());
        assertEquals("rag-agent-service", result.getActionType());
        assertEquals("RuntimeException", result.getFields().get("errorType"));
    }

    @Test
    void equalsAndHashCodeConsiderAllFields() {
        Map<String, Object> fields1 = Map.of("a", 1);
        Map<String, Object> fields2 = Map.of("a", 1);

        AiAnalysisResult r1 = new AiAnalysisResult("T", "A", fields1);
        AiAnalysisResult r2 = new AiAnalysisResult("T", "A", fields2);

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());

        AiAnalysisResult r3 = new AiAnalysisResult("OTHER", "A", fields1);
        assertNotEquals(r1, r3);
    }

    @Test
    void toStringContainsFieldValues() {
        AiAnalysisResult result = new AiAnalysisResult(
                "RAG_ANSWER",
                "rag-agent-service",
                Map.of("k", "v")
        );

        String s = result.toString();
        assertTrue(s.contains("RAG_ANSWER"));
        assertTrue(s.contains("rag-agent-service"));
        assertTrue(s.contains("k"));
        assertTrue(s.contains("v"));
    }
}
