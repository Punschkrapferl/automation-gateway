package com.example.automationgateway.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class DocumentTest {

    @Test
    void constructorSetsIdAndStatusProcessing() {
        String text = "Some raw text";
        Document doc = new Document(text);

        assertNotNull(doc.getId());
        assertEquals(text, doc.getRawText());
        assertEquals(DocumentStatus.PROCESSING, doc.getStatus());
    }

    @Test
    void prePersistSetsCreatedAtAndUpdatedAt() {
        Document doc = new Document("text");
        assertNull(doc.getCreatedAt());
        assertNull(doc.getUpdatedAt());

        doc.prePersist();

        Instant created = doc.getCreatedAt();
        Instant updated = doc.getUpdatedAt();

        assertNotNull(created);
        assertNotNull(updated);
        assertEquals(created, updated, "createdAt and updatedAt should be equal on first persist");
    }

    @Test
    void preUpdateUpdatesUpdatedAtOnly() throws InterruptedException {
        Document doc = new Document("text");
        doc.prePersist();
        Instant created = doc.getCreatedAt();
        Instant updatedBefore = doc.getUpdatedAt();

        // Simulate time passing
        Thread.sleep(5);

        doc.preUpdate();
        Instant updatedAfter = doc.getUpdatedAt();

        assertEquals(created, doc.getCreatedAt());
        assertTrue(updatedAfter.isAfter(updatedBefore));
    }
}
