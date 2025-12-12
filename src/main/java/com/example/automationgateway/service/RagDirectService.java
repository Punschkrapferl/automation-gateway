package com.example.automationgateway.service;

import com.example.automationgateway.dto.RagQueryRequestDTO;
import com.example.automationgateway.dto.RagQueryResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Service responsible for calling the external Python RAG Agent service.
 * <p>
 * This class encapsulates the HTTP communication with the RAG backend and
 * exposes a simple Java method:
 * {@link #query(String, int)}, which internally performs a
 * {@code POST /api/query} request.
 * </p>
 *
 * <p>Configuration:</p>
 * <ul>
 *   <li>The base URL of the RAG service is injected from
 *       {@code ai.service.base-url} (e.g. {@code http://localhost:8000}).</li>
 *   <li>The final query endpoint is computed as
 *       {@code {baseUrl}/api/query}, with trailing slashes normalized.</li>
 * </ul>
 *
 * <p>Example configuration (application.yml):</p>
 * <pre>
 * ai:
 *   service:
 *     base-url: http://localhost:8000
 * </pre>
 *
 * <p>Typical usage from {@link DocumentService}:</p>
 * <pre>
 * RagQueryResponse ragResponse = ragDirectService.query(request.getText(), 5);
 * </pre>
 */
@Service
@Slf4j
public class RagDirectService {

    private final RestTemplate restTemplate;
    private final String queryUrl;

    /**
     * Creates a new {@code RagDirectService}.
     *
     * @param restTemplate the shared {@link RestTemplate} configured in
     *                     {@link com.example.automationgateway.config.RestTemplateConfig}
     * @param baseUrl      base URL of the RAG service, injected from
     *                     {@code ai.service.base-url} (e.g. {@code http://rag-api:8000})
     */
    public RagDirectService(
            RestTemplate restTemplate,
            @Value("${ai.service.base-url}") String baseUrl
    ) {
        this.restTemplate = restTemplate;

        // Normalize base URL to avoid double slashes when appending /api/query
        String normalized = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;
        this.queryUrl = normalized + "/api/query";
        log.info("RAG query URL set to {}", this.queryUrl);
    }

    /**
     * Calls the Python RAG Agent service's {@code POST /api/query} endpoint.
     *
     * <p>Flow:</p>
     * <ol>
     *   <li>Builds a {@link RagQueryRequestDTO} containing {@code question} and {@code topK}</li>
     *   <li>Sets {@code Content-Type} and {@code Accept} to {@code application/json}</li>
     *   <li>Sends the request to {@code {baseUrl}/api/query}</li>
     *   <li>Logs the outgoing request and incoming response</li>
     *   <li>Throws an exception if the HTTP status is not 2xx</li>
     *   <li>Returns the deserialized {@link RagQueryResponseDTO} body</li>
     * </ol>
     *
     * @param question natural-language query to send to the RAG service
     * @param topK     number of documents to retrieve (mapped to {@code top_k} in JSON)
     * @return the RAG service response as {@link RagQueryResponseDTO}
     * @throws RestClientException    if the underlying HTTP call fails
     * @throws IllegalStateException  if the RAG service returns a non-2xx HTTP status
     */
    public RagQueryResponseDTO query(String question, int topK) {
        RagQueryRequestDTO payload = new RagQueryRequestDTO(question, topK);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<RagQueryRequestDTO> entity = new HttpEntity<>(payload, headers);

        log.info("Sending RAG query to {} with body={}", queryUrl, payload);

        ResponseEntity<RagQueryResponseDTO> response;
        try {
            response = restTemplate.postForEntity(
                    queryUrl,
                    entity,
                    RagQueryResponseDTO.class
            );
        } catch (RestClientException e) {
            // Log and rethrow so the caller (DocumentService) can handle the error
            log.error("Error calling RAG service", e);
            throw e;
        }

        log.info("RAG service responded with status={} body={}",
                response.getStatusCode(), response.getBody());

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException(
                    "RAG /api/query returned " + response.getStatusCode()
            );
        }

        return response.getBody();
    }
}
