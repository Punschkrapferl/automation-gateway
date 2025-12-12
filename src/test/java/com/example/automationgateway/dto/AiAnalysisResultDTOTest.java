package com.example.automationgateway.dto;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AiAnalysisResultDTOTest {

    @Test
    void allArgsConstructorAndGettersWork() {
        Map<String, Object> fields = Map.of("key", "value");

        AiAnalysisResultDTO result = new AiAnalysisResultDTO(
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
        AiAnalysisResultDTO result = new AiAnalysisResultDTO();

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

        AiAnalysisResultDTO r1 = new AiAnalysisResultDTO("T", "A", fields1);
        AiAnalysisResultDTO r2 = new AiAnalysisResultDTO("T", "A", fields2);

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());

        AiAnalysisResultDTO r3 = new AiAnalysisResultDTO("OTHER", "A", fields1);
        assertNotEquals(r1, r3);
    }

    @Test
    void toStringContainsFieldValues() {
        AiAnalysisResultDTO result = new AiAnalysisResultDTO(
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
