package com.sekhmet.llmflow.model.dto;

import lombok.Data;
import lombok.Builder;
import java.util.Map;

/**
 * 工作流执行结果 DTO
 */
@Data
@Builder
public class ExecutionResult {
    /** 工作流 ID */
    private String workflowId;
    /** 执行状态: COMPLETED, FAILED */
    private String status;
    /** 各节点的执行结果映射 (NodeID -> Result) */
    private Map<String, NodeResult> nodeResults;
    /** 总耗时 (毫秒) */
    private long totalDurationMs;
}
