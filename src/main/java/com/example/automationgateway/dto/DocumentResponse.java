package com.example.automationgateway.dto;

import com.example.automationgateway.model.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response object returned by the Automation Gateway for document operations.
 * <p>
 * This DTO is used by both:
 * </p>
 * <ul>
 *   <li>{@code POST /api/documents} – after processing a document</li>
 *   <li>{@code GET /api/documents/{id}} – when retrieving a stored document</li>
 * </ul>
 *
 * <p>The fields represent:</p>
 * <ul>
 *   <li>{@code id} – the unique identifier of the persisted document</li>
 *   <li>{@code status} – the processing status ({@code PROCESSING}, {@code COMPLETED}, {@code FAILED})</li>
 *   <li>{@code type} – the semantic result type, e.g. {@code "RAG_ANSWER"} or {@code "ERROR"}</li>
 *   <li>{@code analysis} – the detailed AI analysis result (or {@code null} for invalid/failed results)</li>
 *   <li>{@code createdAt} – timestamp when the document was first saved</li>
 *   <li>{@code updatedAt} – timestamp of the latest update (e.g., after AI processing)</li>
 * </ul>
 *
 * <p>Typical JSON response:</p>
 * <pre>
 * {
 *   "id": "9c0c4d8d-8bf0-4aef-9c3b-9e4be58f4512",
 *   "status": "COMPLETED",
 *   "type": "RAG_ANSWER",
 *   "analysis": {
 *     "type": "RAG_ANSWER",
 *     "actionType": "rag-agent-service",
 *     "fields": {
 *       "query": "What is AI?",
 *       "answer": "Artificial Intelligence is ...",
 *       "documents": [...]
 *     }
 *   },
 *   "createdAt": "2025-01-02T12:33:10Z",
 *   "updatedAt": "2025-01-02T12:33:15Z"
 * }
 * </pre>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {

    /**
     * Unique identifier of the stored document (UUID string).
     */
    private String id;

    /**
     * Processing state of the document in the system.
     * Can be {@code PROCESSING}, {@code COMPLETED}, or {@code FAILED}.
     */
    private DocumentStatus status;

    /**
     * High-level type of the result, usually matching {@link AiAnalysisResult#getType()}.
     */
    private String type;

    /**
     * Analysis output produced by the RAG backend. Can be {@code null} if deserialization fails.
     */
    private AiAnalysisResult analysis;

    /**
     * Timestamp when the document was created.
     */
    private Instant createdAt;

    /**
     * Timestamp when the document was last updated (e.g., after RAG processing).
     */
    private Instant updatedAt;
}
