package com.example.automationgateway.repository;

import com.example.automationgateway.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for managing {@link Document} entities.
 * <p>
 * This repository provides full CRUD support without requiring any manual
 * implementation. By extending {@link JpaRepository}, the following operations
 * become available out-of-the-box:
 * </p>
 *
 * <ul>
 *   <li>{@code save(document)} – insert or update a document</li>
 *   <li>{@code findById(id)} – retrieve a stored document</li>
 *   <li>{@code findAll()} – list all documents</li>
 *   <li>{@code deleteById(id)} – remove a document</li>
 *   <li>automatic pagination and sorting methods</li>
 * </ul>
 *
 * <p>
 * This repository is consumed by {@link com.example.automationgateway.service.DocumentService}
 * for persisting and retrieving document data during the processing lifecycle.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 * Document doc = new Document("example text");
 * documentRepository.save(doc);
 *
 * Optional<Document> stored = documentRepository.findById(doc.getId());
 * </pre>
 */
public interface DocumentRepository extends JpaRepository<Document, String> {
}
