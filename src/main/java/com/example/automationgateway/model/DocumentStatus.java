package com.example.automationgateway.model;

/**
 * Enumeration representing the processing state of a {@link Document}.
 * <p>
 * Each document enters the system as {@code PROCESSING}, and depending on the
 * outcome of the RAG service call inside {@code DocumentService}, transitions
 * into either:
 * </p>
 *
 * <ul>
 *   <li>{@code COMPLETED} – when AI analysis succeeds and a valid
 *       {@code AiAnalysisResult} is generated</li>
 *   <li>{@code FAILED} – when an exception occurs, such as:
 *       <ul>
 *         <li>RAG service unreachable or error response</li>
 *         <li>Serialization/deserialization failures</li>
 *         <li>Unexpected downstream exceptions</li>
 *       </ul>
 *   </li>
 * </ul>
 *
 * <p>
 * This enum is stored in the database as a string (via {@code @Enumerated(EnumType.STRING)})
 * to improve readability and avoid ordinal mismatch issues.
 * </p>
 */
public enum DocumentStatus {

    /**
     * The document was received and is currently being processed.
     */
    PROCESSING,

    /**
     * AI analysis completed successfully.
     */
    COMPLETED,

    /**
     * An error occurred during processing and the document could not be completed.
     */
    FAILED
}
