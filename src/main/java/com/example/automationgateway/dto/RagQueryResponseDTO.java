package com.example.automationgateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response payload returned by the Python RAG Agent service from
 * {@code POST /api/query}.
 * <p>
 * This DTO models the contract expected by the Automation Gateway:
 * </p>
 * <ul>
 *   <li>{@code query} – the original question echoed back by the RAG service</li>
 *   <li>{@code answer} – the synthesized natural-language answer generated
 *       from the retrieved documents</li>
 *   <li>{@code documents} – the list of retrieved passages with scores and metadata</li>
 * </ul>
 *
 * <p>Typical JSON response from the RAG service:</p>
 * <pre>
 * {
 *   "query": "What is AI?",
 *   "answer": "Artificial Intelligence is ...",
 *   "documents": [
 *     {
 *       "id": "doc-1",
 *       "score": 0.92,
 *       "text": "AI is the field of ...",
 *       "metadata": {
 *         "source": "pmcid:PMC123456",
 *         "year": 2023
 *       }
 *     }
 *   ]
 * }
 * </pre>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RagQueryResponseDTO {

    /**
     * The original query that was processed by the RAG service.
     */
    private String query;

    /**
     * Natural-language answer synthesized by the RAG pipeline.
     */
    private String answer;

    /**
     * Ranked list of documents or passages retrieved to support the answer.
     */
    private List<RagRetrievedDocumentDTO> documents;
}
