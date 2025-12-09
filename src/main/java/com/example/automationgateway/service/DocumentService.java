package com.example.automationgateway.service;

import com.example.automationgateway.dto.AiAnalysisResult;
import com.example.automationgateway.dto.DocumentRequest;
import com.example.automationgateway.dto.DocumentResponse;
import com.example.automationgateway.dto.RagQueryResponse;
import com.example.automationgateway.model.Document;
import com.example.automationgateway.model.DocumentStatus;
import com.example.automationgateway.repository.DocumentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Application service responsible for:
 * <ul>
 *   <li>Persisting incoming documents</li>
 *   <li>Calling the external RAG backend for analysis</li>
 *   <li>Storing the AI analysis result as JSON</li>
 *   <li>Mapping entities to API-facing DTOs</li>
 * </ul>
 * <p>
 * Exposed via {@link com.example.automationgateway.controller.DocumentController}.
 * </p>
 */
@Service
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final RagDirectService ragDirectService;
    private final ObjectMapper objectMapper;

    /**
     * Explicit constructor for dependency injection.
     * <p>
     * Using a concrete constructor instead of Lombok's {@code @RequiredArgsConstructor}
     * makes it easier for IDEs to detect the bean correctly and avoids any Lombok
     * configuration issues.
     * </p>
     *
     * @param documentRepository JPA repository for {@link Document} entities
     * @param ragDirectService   client used to call the Python RAG Agent service
     * @param objectMapper       Jackson mapper for JSON (de)serialization
     */
    public DocumentService(
            DocumentRepository documentRepository,
            RagDirectService ragDirectService,
            ObjectMapper objectMapper
    ) {
        this.documentRepository = documentRepository;
        this.ragDirectService = ragDirectService;
        this.objectMapper = objectMapper;
    }

    /**
     * Process a new document: persist it, call the RAG backend, store the
     * analysis result, and return a mapped {@link DocumentResponse}.
     * <p>
     * High-level flow:
     * </p>
     * <ol>
     *   <li>Create a new {@link Document} in {@link DocumentStatus#PROCESSING}</li>
     *   <li>Invoke {@link RagDirectService#query(String, int)} with the document text</li>
     *   <li>Build an {@link AiAnalysisResult} from the RAG response</li>
     *   <li>On success: mark the document as {@link DocumentStatus#COMPLETED}</li>
     *   <li>On failure: mark the document as {@link DocumentStatus#FAILED}
     *       and store error details</li>
     *   <li>Return a DTO with status, type, analysis and timestamps</li>
     * </ol>
     *
     * @param request request DTO containing the raw document text
     * @return fully populated {@link DocumentResponse}
     */
    @Transactional
    public DocumentResponse processDocument(DocumentRequest request) {
        // 1) Persist initial document in PROCESSING
        Document document = new Document();
        document.setId(UUID.randomUUID().toString());
        document.setRawText(request.getText());
        document.setStatus(DocumentStatus.PROCESSING);
        document.setCreatedAt(Instant.now());
        document.setUpdatedAt(Instant.now());
        documentRepository.save(document);

        AiAnalysisResult aiResult;

        try {
            // 2) Call RAG backend
            RagQueryResponse ragResponse = ragDirectService.query(request.getText(), 5);

            // 3) Map RAG response into a generic AiAnalysisResult
            Map<String, Object> fields = new HashMap<>();
            fields.put("query", ragResponse.getQuery());
            fields.put("answer", ragResponse.getAnswer());
            fields.put("documents", ragResponse.getDocuments());

            aiResult = new AiAnalysisResult(
                    "RAG_ANSWER",        // type
                    "rag-agent-service", // actionType / source
                    fields
            );

            document.setType(aiResult.getType());
            document.setStatus(DocumentStatus.COMPLETED);
            document.setAnalysisJson(objectToJson(aiResult));
            document.setUpdatedAt(Instant.now());

        } catch (Exception e) {
            // 4) Any error while calling RAG or serializing → FAILED
            log.error("Error while calling RAG service", e);

            Map<String, Object> errFields = new HashMap<>();
            errFields.put("errorType", e.getClass().getSimpleName());
            errFields.put("message", e.getMessage());

            aiResult = new AiAnalysisResult(
                    "ERROR",
                    "rag-agent-service",
                    errFields
            );

            document.setType(aiResult.getType());
            document.setStatus(DocumentStatus.FAILED);
            document.setAnalysisJson(objectToJson(aiResult));
            document.setUpdatedAt(Instant.now());
        }

        document = documentRepository.save(document);

        // 5) Build response DTO
        DocumentResponse response = new DocumentResponse();
        response.setId(document.getId());
        response.setStatus(document.getStatus());
        response.setType(document.getType());
        response.setAnalysis(aiResult);
        response.setCreatedAt(document.getCreatedAt());
        response.setUpdatedAt(document.getUpdatedAt());

        return response;
    }

    /**
     * Serialize a value into a JSON string.
     * <p>
     * Any {@link JsonProcessingException} is logged and results in a {@code null}
     * return value instead of failing the whole operation.
     * </p>
     *
     * @param value object to serialize
     * @return JSON representation or {@code null} if serialization fails
     */
    private String objectToJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize analysis JSON", e);
            return null;
        }
    }

    /**
     * Look up a document by id and map it to {@link DocumentResponse}.
     * <p>
     * If the stored {@code analysisJson} cannot be deserialized, the response
     * is still returned but with {@code analysis = null}.
     * </p>
     *
     * @param id document identifier
     * @return an {@link Optional} containing the mapped response or empty if no document exists
     */
    public Optional<DocumentResponse> getDocument(String id) {
        return documentRepository.findById(id).map(doc -> {
            AiAnalysisResult analysis = null;
            if (doc.getAnalysisJson() != null) {
                try {
                    analysis = objectMapper.readValue(doc.getAnalysisJson(), AiAnalysisResult.class);
                } catch (JsonProcessingException e) {
                    log.warn("Failed to deserialize analysisJson for document {}", doc.getId(), e);
                }
            }

            DocumentResponse resp = new DocumentResponse();
            resp.setId(doc.getId());
            resp.setStatus(doc.getStatus());
            resp.setType(doc.getType());
            resp.setAnalysis(analysis);
            resp.setCreatedAt(doc.getCreatedAt());
            resp.setUpdatedAt(doc.getUpdatedAt());

            return resp;
        });
    }
}
