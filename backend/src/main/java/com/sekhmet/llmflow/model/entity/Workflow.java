package com.sekhmet.llmflow.model.entity;

import com.sekhmet.llmflow.model.graph.EdgeDefinition;
import com.sekhmet.llmflow.model.graph.NodeDefinition;
import lombok.Data;
import java.util.List;

/**
 * 工作流实体类
 * 定义了工作流的结构，包含节点和边
 */
@Data
public class Workflow {
    /** 工作流 ID */
    private String id;
    /** 工作流名称 */
    private String name;
    /** 节点列表 */
    private List<NodeDefinition> nodes;
    /** 边列表 (连接关系) */
    private List<EdgeDefinition> edges;
}
