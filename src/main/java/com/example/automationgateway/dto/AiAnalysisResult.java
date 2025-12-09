package com.example.automationgateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiAnalysisResult {

    private String type;
    private String actionType;
    private Map<String, Object> fields;
}
