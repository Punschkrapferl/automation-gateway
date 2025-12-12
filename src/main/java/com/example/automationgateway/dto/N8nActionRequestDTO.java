package com.example.automationgateway.dto;

import com.example.automationgateway.model.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload sent from Automation Gateway to n8n.
 * Keeps document metadata plus the full AiAnalysisResult.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class N8nActionRequestDTO {

    /**
     * ID of the document in the Automation Gateway.
     */
    private String documentId;

    /**
     * Current processing status of the document.
     */
    private DocumentStatus status;

    /**
     * High-level action type that n8n should react to.
     * Example: "create_invoice_entry", "create_support_ticket".
     */
    private String actionType;

    /**
     * Result type of the analysis, e.g. "RAG_ANSWER" or "ERROR".
     */
    private String type;

    /**
     * Full AI analysis payload.
     */
    private AiAnalysisResultDTO analysis;
}
