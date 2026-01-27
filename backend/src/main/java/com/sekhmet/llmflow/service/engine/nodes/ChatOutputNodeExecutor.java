package com.sekhmet.llmflow.service.engine.nodes;

import com.sekhmet.llmflow.model.graph.NodeDefinition;
import com.sekhmet.llmflow.service.engine.NodeExecutor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 聊天输出节点执行器
 * 负责收集并格式化聊天输出
 */
@Component
public class ChatOutputNodeExecutor implements NodeExecutor {

    /**
     * 执行输出逻辑
     * 将输入文本拼接并作为最终输出
     * @param node 节点定义
     * @param inputs 输入参数
     * @return 包含 output 字段的结果映射
     */
    @Override
    public Map<String, Object> execute(NodeDefinition node, Map<String, Object> inputs) {
        Map<String, Object> output = new HashMap<>();
        String text = "";

        // 聚合文本输入
        if (inputs.containsKey("text")) {
            text = (String) inputs.get("text");
        } else if (inputs.containsKey("response")) {
            text = (String) inputs.get("response"); // 支持来自 LlmNode 的 response
        } else {
            // 拼接所有字符串
            for (Object val : inputs.values()) {
                if (val instanceof String) {
                    text += (String) val + "\n";
                }
            }
        }

        output.put("output", text.trim());
        return output;
    }

    @Override
    public String getNodeType() {
        return "chat-output-node";
    }
}
