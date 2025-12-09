package com.example.automationgateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Single retrieved document from the RAG Agent Service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RagRetrievedDocument {

    private String id;
    private double score;
    private String text;
    private Map<String, Object> metadata;
}
