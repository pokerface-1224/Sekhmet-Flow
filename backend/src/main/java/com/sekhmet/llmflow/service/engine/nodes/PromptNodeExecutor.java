package com.sekhmet.llmflow.service.engine.nodes;

import com.sekhmet.llmflow.model.graph.NodeDefinition;
import com.sekhmet.llmflow.service.engine.NodeExecutor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 提示词节点执行器
 * 提供静态提示词文本
 */
@Component
public class PromptNodeExecutor implements NodeExecutor {

    /**
     * 执行提示词节点
     * 直接返回配置的文本内容
     * @param node 节点定义
     * @param inputs 输入参数 (忽略)
     * @return 包含 text 的结果映射
     */
    @Override
    public Map<String, Object> execute(NodeDefinition node, Map<String, Object> inputs) {
        Map<String, Object> output = new HashMap<>();
        // In Vue Flow PromptNode, user enters text in 'label' or specific data field
        // Assuming data.label or data.text
        if (node.getData() != null) {
            if (node.getData().containsKey("text")) {
                output.put("text", node.getData().get("text"));
            } else if (node.getData().containsKey("label")) {
                output.put("text", node.getData().get("label"));
            }
        }
        return output;
    }

    @Override
    public String getNodeType() {
        return "prompt-node";
    }
}
