package com.example.automationgateway.dto;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RagRetrievedDocumentDTOTest {

    @Test
    void allArgsConstructorAndGettersWork() {
        Map<String, Object> meta = Map.of("source", "pmcid:PMC123", "page", 5);

        RagRetrievedDocumentDTO doc = new RagRetrievedDocumentDTO(
                "doc-1",
                0.87,
                "some text",
                meta
        );

        assertEquals("doc-1", doc.getId());
        assertEquals(0.87, doc.getScore());
        assertEquals("some text", doc.getText());
        assertEquals(meta, doc.getMetadata());
    }

    @Test
    void noArgsConstructorAndSettersWork() {
        RagRetrievedDocumentDTO doc = new RagRetrievedDocumentDTO();

        doc.setId("doc-2");
        doc.setScore(0.92);
        doc.setText("other text");
        doc.setMetadata(Map.of("k", "v"));

        assertEquals("doc-2", doc.getId());
        assertEquals(0.92, doc.getScore());
        assertEquals("other text", doc.getText());
        assertEquals("v", doc.getMetadata().get("k"));
    }

    @Test
    void equalsAndHashCodeConsiderAllFields() {
        Map<String, Object> meta1 = Map.of("k", "v");
        Map<String, Object> meta2 = Map.of("k", "v");

        RagRetrievedDocumentDTO d1 = new RagRetrievedDocumentDTO(
                "doc-x",
                0.5,
                "text",
                meta1
        );
        RagRetrievedDocumentDTO d2 = new RagRetrievedDocumentDTO(
                "doc-x",
                0.5,
                "text",
                meta2
        );

        assertEquals(d1, d2);
        assertEquals(d1.hashCode(), d2.hashCode());

        RagRetrievedDocumentDTO d3 = new RagRetrievedDocumentDTO(
                "doc-y",
                0.5,
                "text",
                meta1
        );
        assertNotEquals(d1, d3);
    }

    @Test
    void toStringContainsKeyFields() {
        RagRetrievedDocumentDTO doc = new RagRetrievedDocumentDTO(
                "doc-z",
                0.73,
                "snippet",
                Map.of("k", "v")
        );

        String s = doc.toString();
        assertTrue(s.contains("doc-z"));
        assertTrue(s.contains("0.73"));
        assertTrue(s.contains("snippet"));
    }
}
