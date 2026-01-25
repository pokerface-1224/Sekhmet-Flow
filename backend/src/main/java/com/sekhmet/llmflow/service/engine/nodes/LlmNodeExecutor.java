package com.sekhmet.llmflow.service.engine.nodes;

import com.sekhmet.llmflow.config.LlmConfig;
import com.sekhmet.llmflow.model.graph.NodeDefinition;
import com.sekhmet.llmflow.service.LlmService;
import com.sekhmet.llmflow.service.engine.NodeExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class LlmNodeExecutor implements NodeExecutor {

    private final LlmService llmService;

    @Override
    public Map<String, Object> execute(NodeDefinition node, Map<String, Object> inputs) {
        String inputPrompt = "";

        // Simple logic: Concatenate all input texts
        if (inputs.containsKey("text")) {
            inputPrompt = (String) inputs.get("text");
        } else {
            // Append all string values
            for (Object val : inputs.values()) {
                if (val instanceof String) {
                    inputPrompt += (String) val + "\n";
                }
            }
        }

        // Extract overrides from Node Data
        LlmConfig overrideConfig = null;
        Map<String, Object> data = node.getData() != null ? node.getData() : new HashMap<>();

        String provider = (String) data.getOrDefault("provider", "openai"); // Default to openai matching UI
        String modelName = (String) data.get("modelName");
        String apiKey = (String) data.get("apiKey");
        Object tempObj = data.get("temperature");

        // Always create override config to ensure specific node settings (even
        // defaults) are applied
        // and to force validation (e.g. missing key) instead of falling back to
        // potentially uninitialized global default.
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
