package com.sekhmet.llmflow.model.dto;

import lombok.Data;
import lombok.Builder;
import java.util.Map;

@Data
@Builder
public class NodeResult {
    private String nodeId;
    private String status; // SUCCESS, ERROR, SKIPPED
    private Map<String, Object> inputs;
    private Map<String, Object> outputs;
    private String error;
    private long durationMs;
}
