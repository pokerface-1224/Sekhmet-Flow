package com.sekhmet.llmflow.model.graph;

import lombok.Data;

/**
 * 边定义类
 * 描述工作流图中节点之间的连接
 */
@Data
public class EdgeDefinition {
    /** 边 ID */
    private String id;
    /** 源节点 ID */
    private String source;
    /** 目标节点 ID */
    private String target;
    /** 源节点句柄 (连接点) */
    private String sourceHandle;
    /** 目标节点句柄 (连接点) */
    private String targetHandle;
}
