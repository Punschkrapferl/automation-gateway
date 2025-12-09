package com.example.automationgateway.dto;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RagQueryResponseTest {

    @Test
    void allArgsConstructorAndGettersWork() {
        RagRetrievedDocument doc1 = new RagRetrievedDocument(
                "doc-1",
                0.95,
                "some text",
                Map.of("k", "v")
        );
        RagRetrievedDocument doc2 = new RagRetrievedDocument(
                "doc-2",
                0.90,
                "other text",
                Map.of("k2", "v2")
        );

        RagQueryResponse resp = new RagQueryResponse(
                "query text",
                "answer text",
                List.of(doc1, doc2)
        );

        assertEquals("query text", resp.getQuery());
        assertEquals("answer text", resp.getAnswer());
        assertNotNull(resp.getDocuments());
        assertEquals(2, resp.getDocuments().size());
        assertEquals("doc-1", resp.getDocuments().get(0).getId());
        assertEquals("doc-2", resp.getDocuments().get(1).getId());
    }

    @Test
    void noArgsConstructorAndSettersWork() {
        RagRetrievedDocument doc = new RagRetrievedDocument(
                "doc-1",
                0.8,
                "text",
                Map.of("k", "v")
        );

        RagQueryResponse resp = new RagQueryResponse();
        resp.setQuery("q");
        resp.setAnswer("a");
        resp.setDocuments(List.of(doc));

        assertEquals("q", resp.getQuery());
        assertEquals("a", resp.getAnswer());
        assertEquals(1, resp.getDocuments().size());
        assertEquals("doc-1", resp.getDocuments().get(0).getId());
    }

    @Test
    void equalsAndHashCodeConsiderAllFields() {
        RagRetrievedDocument doc1 = new RagRetrievedDocument(
                "doc-1",
                0.9,
                "t",
                Map.of("k", "v")
        );
        RagRetrievedDocument doc2 = new RagRetrievedDocument(
                "doc-1",
                0.9,
                "t",
                Map.of("k", "v")
        );

        RagQueryResponse r1 = new RagQueryResponse(
                "q",
                "a",
                List.of(doc1)
        );
        RagQueryResponse r2 = new RagQueryResponse(
                "q",
                "a",
                List.of(doc2)
        );

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());

        RagQueryResponse r3 = new RagQueryResponse(
                "q-different",
                "a",
                List.of(doc1)
        );

        assertNotEquals(r1, r3);
    }

    @Test
    void toStringContainsKeyFields() {
        RagRetrievedDocument doc = new RagRetrievedDocument(
                "doc-x",
                0.7,
                "some text",
                Map.of()
        );

        RagQueryResponse resp = new RagQueryResponse(
                "query-x",
                "answer-x",
                List.of(doc)
        );

        String s = resp.toString();
        assertTrue(s.contains("query-x"));
        assertTrue(s.contains("answer-x"));
        assertTrue(s.contains("doc-x"));
    }
}
