package com.example.automationgateway.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for creating or processing a document.
 * <p>
 * This DTO is used by {@code POST /api/documents} and represents the minimal
 * input required by the Automation Gateway: a raw text string that will be
 * forwarded to the RAG backend for semantic processing.
 * </p>
 *
 * <p>Validation:</p>
 * <ul>
 *   <li>{@code text} must not be {@code null}, empty, or whitespace-only
 *       (enforced via {@link NotBlank}).</li>
 * </ul>
 *
 * <p>Example JSON:</p>
 * <pre>
 * {
 *   "text": "What is the capital of France?"
 * }
 * </pre>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentRequestDTO {

    /**
     * Raw text input provided by the client. This is the string that will be
     * sent to the RAG service for analysis.
     */
    @NotBlank
    private String text;
}
