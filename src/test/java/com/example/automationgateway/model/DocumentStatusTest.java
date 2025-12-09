package com.example.automationgateway.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DocumentStatusTest {

    @Test
    void valuesContainAllExpectedConstantsInOrder() {
        DocumentStatus[] values = DocumentStatus.values();

        assertEquals(3, values.length);
        assertEquals(DocumentStatus.PROCESSING, values[0]);
        assertEquals(DocumentStatus.COMPLETED, values[1]);
        assertEquals(DocumentStatus.FAILED, values[2]);
    }

    @Test
    void valueOfReturnsCorrectEnum() {
        assertSame(DocumentStatus.PROCESSING, DocumentStatus.valueOf("PROCESSING"));
        assertSame(DocumentStatus.COMPLETED, DocumentStatus.valueOf("COMPLETED"));
        assertSame(DocumentStatus.FAILED, DocumentStatus.valueOf("FAILED"));
    }

    @Test
    void valueOfWithInvalidNameThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> DocumentStatus.valueOf("UNKNOWN"));
    }
}
