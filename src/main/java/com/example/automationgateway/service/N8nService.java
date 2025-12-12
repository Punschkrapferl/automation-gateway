package com.example.automationgateway.service;

import com.example.automationgateway.dto.AiAnalysisResultDTO;
import com.example.automationgateway.dto.N8nActionRequestDTO;
import com.example.automationgateway.model.Document;
import com.example.automationgateway.model.DocumentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Client responsible for sending AI actions to n8n via its webhook.
 * Failures are logged but do NOT break the main document flow.
 */
@Service
@Slf4j
public class N8nService {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String webhookPath;

    public N8nService(
            RestTemplate restTemplate,
            @Value("${n8n.base-url}") String baseUrl,
            @Value("${n8n.webhook-path:/webhook/ai-action}") String webhookPath
    ) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.webhookPath = webhookPath;
    }

    /**
     * Sends an AI action to n8n if an actionType is present.
     *
     * @param document the processed document entity
     * @param analysis the AI analysis result
     */
    public void sendAiAction(Document document, AiAnalysisResultDTO analysis) {
        if (analysis == null) {
            log.debug("Skipping n8n call: analysis is null for document {}", document.getId());
            return;
        }

        String actionType = analysis.getActionType();
        if (actionType == null || actionType.isBlank()) {
            log.debug("Skipping n8n call: no actionType for document {}", document.getId());
            return;
        }

        N8nActionRequestDTO payload = new N8nActionRequestDTO(
                document.getId(),
                document.getStatus() != null ? document.getStatus() : DocumentStatus.COMPLETED,
                actionType,
                analysis.getType(),
                analysis
        );

        String url = baseUrl + webhookPath;

        try {
            log.info("Sending AI action '{}' for document {} to n8n at {}",
                    actionType, document.getId(), url);

            HttpEntity<N8nActionRequestDTO> entity = new HttpEntity<>(payload);
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            log.info("n8n response for document {}: status={} body={}",
                    document.getId(), response.getStatusCode(), response.getBody());
        } catch (Exception ex) {
            log.warn("Failed to send AI action to n8n for document {}: {}",
                    document.getId(), ex.getMessage(), ex);
        }
    }
}
