package com.example.automationgateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Represents a single retrieved passage or document returned by the
 * Python RAG Agent service as part of {@link RagQueryResponse}.
 * <p>
 * Each instance contains:
 * </p>
 * <ul>
 *   <li>{@code id} – unique document or chunk identifier (depends on backend)</li>
 *   <li>{@code score} – relevance score assigned by the retriever model</li>
 *   <li>{@code text} – the retrieved text passage</li>
 *   <li>{@code metadata} – optional structured fields such as source information,
 *       timestamps, PMCID/PMID links, section names, etc.</li>
 * </ul>
 *
 * <p>Example JSON entry:</p>
 * <pre>
 * {
 *   "id": "chunk-347",
 *   "score": 0.89,
 *   "text": "Artificial Intelligence refers to...",
 *   "metadata": {
 *     "source": "pmcid:PMC9023121",
 *     "section": "introduction",
 *     "year": 2023
 *   }
 * }
 * </pre>
 *
 * <p>
 * The Automation Gateway stores these retrieved documents inside
 * {@code AiAnalysisResult.fields["documents"]} so that downstream clients
 * can display or further process the RAG evidence.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RagRetrievedDocument {

    /**
     * Unique identifier of the retrieved document or passage.
     * This may correspond to a chunk ID, PMCID, or backend-specific identifier.
     */
    private String id;

    /**
     * Relevance score returned by the retrieval model.
     * Higher scores indicate stronger similarity to the query.
     */
    private double score;

    /**
     * The textual content of the retrieved passage.
     */
    private String text;

    /**
     * Optional metadata associated with the retrieved document.
     * May include attributes like source, section, year, dataset IDs, etc.
     */
    private Map<String, Object> metadata;
}
