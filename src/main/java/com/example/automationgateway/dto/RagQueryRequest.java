package com.example.automationgateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload used when calling the Python RAG Agent service
 * via {@code POST /api/query}.
 * <p>
 * The Automation Gateway sends this DTO using {@link com.example.automationgateway.service.RagDirectService},
 * which serializes it to JSON and forwards it to the external RAG backend.
 * </p>
 *
 * <p>Field description:</p>
 * <ul>
 *   <li>{@code query} – the natural-language question provided by the user</li>
 *   <li>{@code topK} – the number of retrieved documents the RAG service
 *       should return (mapped to JSON as {@code top_k})</li>
 * </ul>
 *
 * <p>Example JSON sent to the RAG service:</p>
 * <pre>
 * {
 *   "query": "Explain quantum entanglement.",
 *   "top_k": 5
 * }
 * </pre>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RagQueryRequest {

    /**
     * Natural-language query sent to the RAG Agent.
     */
    private String query;

    /**
     * Maximum number of retrieved documents to return.
     * Serialized as {@code top_k} to match the Python service contract.
     */
    @JsonProperty("top_k")
    private int topK;
}
