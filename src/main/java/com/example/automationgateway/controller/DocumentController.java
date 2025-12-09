package com.example.automationgateway.controller;

import com.example.automationgateway.dto.DocumentRequest;
import com.example.automationgateway.dto.DocumentResponse;
import com.example.automationgateway.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REST controller that exposes endpoints for creating and retrieving documents.
 * <p>
 * The controller delegates business logic to {@link DocumentService} and
 * returns DTOs tailored for the API layer.
 * </p>
 */
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    /**
     * Service responsible for document persistence and RAG-based processing.
     */
    private final DocumentService documentService;

    /**
     * Create and process a new document.
     * <p>
     * The request body must contain the raw document text. The document is
     * persisted in the database, sent to the RAG backend for analysis, and
     * the resulting {@link DocumentResponse} is returned.
     * </p>
     *
     * @param request validated request containing the document text
     * @return HTTP 200 with the processed document response
     */
    @PostMapping
    public ResponseEntity<DocumentResponse> createDocument(
            @Valid @RequestBody DocumentRequest request
    ) {
        DocumentResponse response = documentService.processDocument(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Fetch a previously processed document by its identifier.
     *
     * @param id document identifier (UUID string)
     * @return HTTP 200 with the document if found, or HTTP 404 otherwise
     */
    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getDocument(@PathVariable String id) {
        Optional<DocumentResponse> response = documentService.getDocument(id);
        return response.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
