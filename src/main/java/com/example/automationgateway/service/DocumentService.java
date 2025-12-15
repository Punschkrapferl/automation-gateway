package com.example.automationgateway.service;

import com.example.automationgateway.dto.AiAnalysisResultDTO;
import com.example.automationgateway.dto.DocumentRequestDTO;
import com.example.automationgateway.dto.DocumentResponseDTO;
import com.example.automationgateway.dto.RagQueryResponseDTO;
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
 *   <li>Triggering downstream automations (n8n)</li>
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
    private final N8nService n8NService;

    /**
     * Explicit constructor for dependency injection.
     *
     * @param documentRepository JPA repository for {@link Document} entities
     * @param ragDirectService   client used to call the Python RAG Agent service
     * @param objectMapper       Jackson mapper for JSON (de)serialization
     * @param n8NService         client used to send automation events to n8n
     */
    public DocumentService(
            DocumentRepository documentRepository,
            RagDirectService ragDirectService,
            ObjectMapper objectMapper,
            N8nService n8NService
    ) {
        this.documentRepository = documentRepository;
        this.ragDirectService = ragDirectService;
        this.objectMapper = objectMapper;
        this.n8NService = n8NService;
    }

    /**
     * Process a new document: persist it, call the RAG backend, store the
     * analysis result, optionally trigger n8n, and return a mapped {@link DocumentResponseDTO}.
     */
    @Transactional
    public DocumentResponseDTO processDocument(DocumentRequestDTO request) {
        // 1) Persist initial document in PROCESSING
        Document document = new Document();
        document.setId(UUID.randomUUID().toString());
        document.setRawText(request.getText());
        document.setStatus(DocumentStatus.PROCESSING);
        document.setCreatedAt(Instant.now());
        document.setUpdatedAt(Instant.now());
        documentRepository.save(document);

        AiAnalysisResultDTO aiResult;

        try {
            // 2) Call RAG backend
            RagQueryResponseDTO ragResponse = ragDirectService.query(request.getText(), 5);

            // 3) Map RAG response into a generic AiAnalysisResult
            Map<String, Object> fields = new HashMap<>();
            fields.put("query", ragResponse.getQuery());
            fields.put("answer", ragResponse.getAnswer());
            fields.put("documents", ragResponse.getDocuments());
            fields.put("source", "rag-agent-service");

            // IMPORTANT: actionType encodes the business action for n8n
            // Example: "create_invoice_entry" or "create_support_ticket"
            aiResult = new AiAnalysisResultDTO(
                    "RAG_ANSWER",           // type
                    "create_invoice_entry", // actionType (used by n8n Switch node)
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
            // Optional extras that can be used in ClickUp if you want:
            errFields.put("title", "Error while processing document " + document.getId());
            errFields.put("description",
                    "An exception occurred in the RAG pipeline:\n"
                            + e.getClass().getSimpleName() + ": " + e.getMessage());

            // Re-use the support-ticket branch in n8n for errors
            aiResult = new AiAnalysisResultDTO(
                    "ERROR",
                    "create_support_ticket", // was "notify_error"
                    errFields
            );

            document.setType(aiResult.getType());
            document.setStatus(DocumentStatus.FAILED);
            document.setAnalysisJson(objectToJson(aiResult));
            document.setUpdatedAt(Instant.now());
        }

        // 5) Persist final state
        document = documentRepository.save(document);

        // 6) Trigger n8n automation (safe: errors are logged inside N8nClient)
        n8NService.sendAiAction(document, aiResult);

        // 7) Build response DTO
        DocumentResponseDTO response = new DocumentResponseDTO();
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
     * Look up a document by id and map it to {@link DocumentResponseDTO}.
     */
    public Optional<DocumentResponseDTO> getDocument(String id) {
        return documentRepository.findById(id).map(doc -> {
            AiAnalysisResultDTO analysis = null;
            if (doc.getAnalysisJson() != null) {
                try {
                    analysis = objectMapper.readValue(doc.getAnalysisJson(), AiAnalysisResultDTO.class);
                } catch (JsonProcessingException e) {
                    log.warn("Failed to deserialize analysisJson for document {}", doc.getId(), e);
                }
            }

            DocumentResponseDTO resp = new DocumentResponseDTO();
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
