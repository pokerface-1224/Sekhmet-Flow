package com.sekhmet.llmflow.service.engine.nodes;

import com.sekhmet.llmflow.model.graph.NodeDefinition;
import com.sekhmet.llmflow.service.engine.NodeExecutor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 思维链输出节点执行器
 * 负责收集并展示模型的推理思维链 (reasoning_content)
 */
@Component
public class ThinkingOutputNodeExecutor implements NodeExecutor {

    /**
     * 执行输出逻辑
     * 从输入中提取 thinkingContent 作为输出展示
     * 
     * @param node   节点定义
     * @param inputs 输入参数
     * @return 包含 thinkingContent 字段的结果映射
     */
    @Override
    public Map<String, Object> execute(NodeDefinition node, Map<String, Object> inputs) {
        Map<String, Object> output = new HashMap<>();
        String thinkingContent = "";

        // 优先提取 thinkingContent
        if (inputs.containsKey("thinkingContent")) {
            thinkingContent = (String) inputs.get("thinkingContent");
        } else {
            // 如果没有显式的 thinkingContent，尝试从其他字段获取
            for (Map.Entry<String, Object> entry : inputs.entrySet()) {
                String key = entry.getKey();
                // 跳过内部字段和非思维链字段
                if (key.startsWith("_") || key.equals("response") || key.equals("text") || key.equals("output")) {
                    continue;
                }
                if (entry.getValue() instanceof String) {
                    thinkingContent = (String) entry.getValue();
                    break;
                }
            }
        }

        output.put("thinkingContent", thinkingContent.trim());
        return output;
    }

    @Override
    public String getNodeType() {
        return "thinking-output-node";
    }
}
