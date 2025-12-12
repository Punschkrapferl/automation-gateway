package com.example.automationgateway.dto;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RagQueryResponseDTOTest {

    @Test
    void allArgsConstructorAndGettersWork() {
        RagRetrievedDocumentDTO doc1 = new RagRetrievedDocumentDTO(
                "doc-1",
                0.95,
                "some text",
                Map.of("k", "v")
        );
        RagRetrievedDocumentDTO doc2 = new RagRetrievedDocumentDTO(
                "doc-2",
                0.90,
                "other text",
                Map.of("k2", "v2")
        );

        RagQueryResponseDTO resp = new RagQueryResponseDTO(
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
        RagRetrievedDocumentDTO doc = new RagRetrievedDocumentDTO(
                "doc-1",
                0.8,
                "text",
                Map.of("k", "v")
        );

        RagQueryResponseDTO resp = new RagQueryResponseDTO();
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
        RagRetrievedDocumentDTO doc1 = new RagRetrievedDocumentDTO(
                "doc-1",
                0.9,
                "t",
                Map.of("k", "v")
        );
        RagRetrievedDocumentDTO doc2 = new RagRetrievedDocumentDTO(
                "doc-1",
                0.9,
                "t",
                Map.of("k", "v")
        );

        RagQueryResponseDTO r1 = new RagQueryResponseDTO(
                "q",
                "a",
                List.of(doc1)
        );
        RagQueryResponseDTO r2 = new RagQueryResponseDTO(
                "q",
                "a",
                List.of(doc2)
        );

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());

        RagQueryResponseDTO r3 = new RagQueryResponseDTO(
                "q-different",
                "a",
                List.of(doc1)
        );

        assertNotEquals(r1, r3);
    }

    @Test
    void toStringContainsKeyFields() {
        RagRetrievedDocumentDTO doc = new RagRetrievedDocumentDTO(
                "doc-x",
                0.7,
                "some text",
                Map.of()
        );

        RagQueryResponseDTO resp = new RagQueryResponseDTO(
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
