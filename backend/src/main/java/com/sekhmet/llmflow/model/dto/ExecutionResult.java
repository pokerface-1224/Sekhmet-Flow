package com.sekhmet.llmflow.model.dto;

import lombok.Data;
import lombok.Builder;
import java.util.Map;

@Data
@Builder
public class ExecutionResult {
    private String workflowId;
    private String status; // COMPLETED, FAILED
    private Map<String, NodeResult> nodeResults;
    private long totalDurationMs;
}
