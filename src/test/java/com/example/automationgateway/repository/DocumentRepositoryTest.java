package com.example.automationgateway.repository;

import com.example.automationgateway.model.Document;
import com.example.automationgateway.model.DocumentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class DocumentRepositoryTest {

    @Autowired
    private DocumentRepository documentRepository;

    @Test
    void saveAndFindById() {
        Document doc = new Document("hello world");
        doc.setStatus(DocumentStatus.PROCESSING);

        Document saved = documentRepository.save(doc);
        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());

        Optional<Document> found = documentRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("hello world", found.get().getRawText());
        assertEquals(DocumentStatus.PROCESSING, found.get().getStatus());
    }
}
