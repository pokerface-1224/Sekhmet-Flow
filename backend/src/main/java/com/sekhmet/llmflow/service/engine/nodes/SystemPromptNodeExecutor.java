package com.sekhmet.llmflow.service.engine.nodes;

import com.sekhmet.llmflow.model.graph.NodeDefinition;
import com.sekhmet.llmflow.service.engine.NodeExecutor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统提示词节点执行器
 * 提供系统级指令文本
 */
@Component
public class SystemPromptNodeExecutor implements NodeExecutor {

    /**
     * 执行系统提示词节点
     * 返回配置的系统提示词内容
     * 
     * @param node   节点定义
     * @param inputs 输入参数 (忽略)
     * @return 包含 systemPrompt 的结果映射
     */
    @Override
    public Map<String, Object> execute(NodeDefinition node, Map<String, Object> inputs) {
        Map<String, Object> output = new HashMap<>();
        if (node.getData() != null) {
            if (node.getData().containsKey("text")) {
                output.put("systemPrompt", node.getData().get("text"));
            } else if (node.getData().containsKey("label")) {
                output.put("systemPrompt", node.getData().get("label"));
            }
        }
        return output;
    }

    @Override
    public String getNodeType() {
        return "system-prompt-node";
    }
}
