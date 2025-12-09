package com.example.automationgateway.dto;

import com.example.automationgateway.model.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {

    private String id;
    private DocumentStatus status;
    private String type;
    private AiAnalysisResult analysis;
    private Instant createdAt;
    private Instant updatedAt;
}
