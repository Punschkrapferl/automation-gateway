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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final RagDirectService ragDirectService;
    private final ObjectMapper objectMapper;

    /**
     * Called from POST /api/documents.
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
                    "RAG_ANSWER",          // type
                    "rag-agent-service",   // actionType / source
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

    private String objectToJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize analysis JSON", e);
            return null;
        }
    }

    /**
     * Called from GET /api/documents/{id}.
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
