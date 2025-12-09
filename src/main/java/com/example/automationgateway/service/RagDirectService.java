package com.example.automationgateway.service;

import com.example.automationgateway.dto.RagQueryRequest;
import com.example.automationgateway.dto.RagQueryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@Slf4j
public class RagDirectService {

    private final RestTemplate restTemplate;
    private final String queryUrl;

    public RagDirectService(
            RestTemplate restTemplate,
            @Value("${ai.service.base-url}") String baseUrl
    ) {
        this.restTemplate = restTemplate;

        String normalized = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;
        this.queryUrl = normalized + "/api/query";
        log.info("RAG query URL set to {}", this.queryUrl);
    }

    /**
     * Call the Python rag-agent-service's POST /api/query endpoint.
     */
    public RagQueryResponse query(String question, int topK) {
        RagQueryRequest payload = new RagQueryRequest(question, topK);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<RagQueryRequest> entity = new HttpEntity<>(payload, headers);

        log.info("Sending RAG query to {} with body={}", queryUrl, payload);

        ResponseEntity<RagQueryResponse> response;
        try {
            response = restTemplate.postForEntity(
                    queryUrl,
                    entity,
                    RagQueryResponse.class
            );
        } catch (RestClientException e) {
            // LOG and rethrow the original cause so DocumentService can see it
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
