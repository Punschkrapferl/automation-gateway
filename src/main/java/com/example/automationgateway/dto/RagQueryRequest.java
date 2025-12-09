package com.example.automationgateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for the RAG Agent Service /api/query endpoint.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RagQueryRequest {

    private String query;

    @JsonProperty("top_k")
    private int topK;
}
