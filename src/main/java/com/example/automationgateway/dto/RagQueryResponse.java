package com.example.automationgateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response body from the RAG Agent Service /api/query endpoint.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RagQueryResponse {

    private String query;
    private String answer;
    private List<RagRetrievedDocument> documents;
}
