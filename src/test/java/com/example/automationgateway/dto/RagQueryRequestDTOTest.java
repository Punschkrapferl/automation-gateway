package com.example.automationgateway.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RagQueryRequestDTOTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void allArgsConstructorAndGettersWork() {
        RagQueryRequestDTO req = new RagQueryRequestDTO("some query", 5);

        assertEquals("some query", req.getQuery());
        assertEquals(5, req.getTopK());
    }

    @Test
    void noArgsConstructorAndSettersWork() {
        RagQueryRequestDTO req = new RagQueryRequestDTO();
        req.setQuery("hello");
        req.setTopK(3);

        assertEquals("hello", req.getQuery());
        assertEquals(3, req.getTopK());
    }

    @Test
    void serializesTopKAsTopKSnakeCase() throws Exception {
        RagQueryRequestDTO req = new RagQueryRequestDTO("test query", 7);

        String json = objectMapper.writeValueAsString(req);

        assertTrue(json.contains("\"query\":\"test query\""));
        assertTrue(json.contains("\"top_k\":7"));
        assertFalse(json.contains("topK"));
    }

    @Test
    void deserializesTopKFromSnakeCase() throws Exception {
        String json = """
                {
                  "query": "another query",
                  "top_k": 10
                }
                """;

        RagQueryRequestDTO req = objectMapper.readValue(json, RagQueryRequestDTO.class);

        assertEquals("another query", req.getQuery());
        assertEquals(10, req.getTopK());
    }
}
