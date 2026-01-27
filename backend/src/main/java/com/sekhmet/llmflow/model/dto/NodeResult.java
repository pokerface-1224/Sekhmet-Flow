package com.sekhmet.llmflow.model.dto;

import lombok.Data;
import lombok.Builder;
import java.util.Map;

/**
 * 节点执行结果 DTO
 */
@Data
@Builder
public class NodeResult {
    /** 节点 ID */
    private String nodeId;
    /** 节点状态: SUCCESS, ERROR, SKIPPED */
    private String status;
    /** 节点输入参数 */
    private Map<String, Object> inputs;
    /** 节点输出结果 */
    private Map<String, Object> outputs;
    /** 错误信息 (如果有) */
    private String error;
    /** 执行耗时 (毫秒) */
    private long durationMs;
}
