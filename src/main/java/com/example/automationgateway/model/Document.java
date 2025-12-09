package com.example.automationgateway.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity representing a processed document within the Automation Gateway.
 * <p>
 * Every inbound request from {@code POST /api/documents} is persisted first with
 * an initial {@link DocumentStatus#PROCESSING} state. After the RAG analysis
 * succeeds or fails, this entity is updated with the AI result metadata.
 * </p>
 *
 * <p>Mapped database fields:</p>
 * <ul>
 *   <li>{@code id} – UUID string (primary key)</li>
 *   <li>{@code rawText} – original submitted text or prompt</li>
 *   <li>{@code status} – current processing state</li>
 *   <li>{@code type} – semantic classification or result type
 *       (e.g. {@code "RAG_ANSWER"}, {@code "ERROR"})</li>
 *   <li>{@code analysisJson} – serialized {@code AiAnalysisResult}</li>
 *   <li>{@code createdAt} – timestamp when the entity was first saved</li>
 *   <li>{@code updatedAt} – timestamp when the entity was last modified</li>
 * </ul>
 *
 * <p>Lifecycle behavior:</p>
 * <ul>
 *   <li>{@link #prePersist()} sets both {@code createdAt} and {@code updatedAt}</li>
 *   <li>{@link #preUpdate()} updates only {@code updatedAt}</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>
 * Document doc = new Document("Explain gravity.");
 * repository.save(doc);
 * // later...
 * doc.setStatus(DocumentStatus.COMPLETED);
 * doc.setAnalysisJson("{ ... }");
 * repository.save(doc);
 * </pre>
 */
@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Document {

    /**
     * Primary key (UUID stored as a 36-character string).
     */
    @Id
    @Column(length = 36)
    private String id;

    /**
     * Raw user-provided text that is being processed.
     */
    @Lob
    @Column(name = "raw_text", nullable = false)
    private String rawText;

    /**
     * Current processing status of the document.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus status;

    /**
     * Semantic output type (e.g., "RAG_ANSWER" or "ERROR").
     */
    @Column(name = "doc_type")
    private String type;

    /**
     * Serialized {@code AiAnalysisResult} as JSON.
     * Stored as a CLOB due to variable size of responses.
     */
    @Lob
    @Column(name = "analysis_json")
    private String analysisJson;

    /**
     * Timestamp when the record was created.
     * Set automatically on first persistence.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Timestamp when the record was last updated.
     * Updated automatically on every modification.
     */
    @Column(name = "updated_at")
    private Instant updatedAt;

    /**
     * Convenience constructor for creating a new document based on raw text.
     * The ID and initial status are assigned automatically.
     *
     * @param rawText the text to be analyzed
     */
    public Document(String rawText) {
        this.id = UUID.randomUUID().toString();
        this.rawText = rawText;
        this.status = DocumentStatus.PROCESSING;
    }

    /**
     * Automatically sets timestamps when the entity is inserted.
     */
    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * Automatically updates the timestamp on updates.
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
