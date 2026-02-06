package com.sekhmet.llmflow.service.engine;

import com.sekhmet.llmflow.model.graph.NodeDefinition;
import java.util.Map;

/**
 * 节点执行器接口
 * 定义了具体节点逻辑的执行约定
 */
public interface NodeExecutor {
    /**
     * 执行节点逻辑
     * @param node 节点定义
     * @param inputs 输入参数 (来自前序节点)
     * @return 节点输出结果
     */
    Map<String, Object> execute(NodeDefinition node, Map<String, Object> inputs);

    /**
     * 获取此执行器处理的节点类型
     * @return 节点类型字符串 (如 "llm-node")
     */
    String getNodeType();
}
