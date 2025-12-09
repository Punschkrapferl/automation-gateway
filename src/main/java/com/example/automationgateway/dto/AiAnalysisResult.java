package com.example.automationgateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Generic wrapper for AI analysis results returned by the backend.
 * <p>
 * This DTO is intentionally flexible so that different types of analysis
 * (e.g. RAG answers, classification results, error reports) can be represented
 * in a uniform way and attached to a {@code DocumentResponse}.
 * </p>
 *
 * <p>Typical usage in this project:</p>
 * <ul>
 *   <li>{@code type} – high-level category of the result
 *     <ul>
 *       <li>{@code "RAG_ANSWER"} for successful responses from the RAG service</li>
 *       <li>{@code "ERROR"} for failures when calling the RAG service</li>
 *     </ul>
 *   </li>
 *   <li>{@code actionType} – logical source or processor that produced the result
 *     <ul>
 *       <li>For example: {@code "rag-agent-service"}</li>
 *     </ul>
 *   </li>
 *   <li>{@code fields} – arbitrary key/value payload with structured data
 *     <ul>
 *       <li>For RAG results: {@code "query"}, {@code "answer"}, {@code "documents"}</li>
 *       <li>For errors: {@code "errorType"}, {@code "message"}</li>
 *     </ul>
 *   </li>
 * </ul>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiAnalysisResult {

    /**
     * High-level type of the analysis result, e.g. {@code "RAG_ANSWER"} or {@code "ERROR"}.
     */
    private String type;

    /**
     * Logical source or action that produced the analysis, e.g. {@code "rag-agent-service"}.
     */
    private String actionType;

    /**
     * Flexible map of structured data associated with this result.
     * The concrete keys depend on {@link #type} and {@link #actionType}.
     */
    private Map<String, Object> fields;
}
