package com.sekhmet.llmflow.service.engine.nodes;

import com.sekhmet.llmflow.config.LlmConfig;
import com.sekhmet.llmflow.model.graph.NodeDefinition;
import com.sekhmet.llmflow.service.LlmService;
import com.sekhmet.llmflow.service.engine.NodeExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * LLM 节点执行器
 * 调用 LLM 服务生成回复
 */
@Component
@RequiredArgsConstructor
public class LlmNodeExecutor implements NodeExecutor {

    private final LlmService llmService;

    /**
     * 执行 LLM 调用
     * 从输入中获取提示词，并应用节点特定的配置覆盖
     * @param node 节点定义 (包含模型配置)
     * @param inputs 输入参数 (包含 prompt 文本)
     * @return 包含 response 和 text 的结果映射
     */
    @Override
    public Map<String, Object> execute(NodeDefinition node, Map<String, Object> inputs) {
        String inputPrompt = "";

        // 简单逻辑: 拼接所有输入文本
        if (inputs.containsKey("text")) {
            inputPrompt = (String) inputs.get("text");
        } else {
            // 追加所有字符串值
            for (Object val : inputs.values()) {
                if (val instanceof String) {
                    inputPrompt += (String) val + "\n";
                }
            }
        }

        // 从节点数据中提取配置覆盖
        LlmConfig overrideConfig = null;
        Map<String, Object> data = node.getData() != null ? node.getData() : new HashMap<>();

        String provider = (String) data.getOrDefault("provider", "openai"); // 默认为 openai
        String modelName = (String) data.get("modelName");
        String apiKey = (String) data.get("apiKey");
        Object tempObj = data.get("temperature");

        // 总是创建覆盖配置，以确保应用节点特定设置 (即使是默认值)
        // 并且强制验证 (例如缺少 key)，而不是回退到可能未初始化的全局默认值。
        overrideConfig = new LlmConfig();
        overrideConfig.setProvider(provider);
        if (modelName != null && !modelName.isEmpty())
            overrideConfig.setModelName(modelName);
        if (apiKey != null && !apiKey.isEmpty())
            overrideConfig.setApiKey(apiKey);
        if (tempObj != null) {
            try {
                overrideConfig.setTemperature(Double.parseDouble(tempObj.toString()));
            } catch (NumberFormatException ignored) {
            }
        }

        String response = llmService.generate(inputPrompt, overrideConfig);

        Map<String, Object> output = new HashMap<>();
        output.put("response", response);
        output.put("text", response);
        return output;
    }

    @Override
    public String getNodeType() {
        return "llm-node";
    }
}
