package com.example.automationgateway.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RagQueryRequestTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void allArgsConstructorAndGettersWork() {
        RagQueryRequest req = new RagQueryRequest("some query", 5);

        assertEquals("some query", req.getQuery());
        assertEquals(5, req.getTopK());
    }

    @Test
    void noArgsConstructorAndSettersWork() {
        RagQueryRequest req = new RagQueryRequest();
        req.setQuery("hello");
        req.setTopK(3);

        assertEquals("hello", req.getQuery());
        assertEquals(3, req.getTopK());
    }

    @Test
    void serializesTopKAsTopKSnakeCase() throws Exception {
        RagQueryRequest req = new RagQueryRequest("test query", 7);

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

        RagQueryRequest req = objectMapper.readValue(json, RagQueryRequest.class);

        assertEquals("another query", req.getQuery());
        assertEquals(10, req.getTopK());
    }
}
