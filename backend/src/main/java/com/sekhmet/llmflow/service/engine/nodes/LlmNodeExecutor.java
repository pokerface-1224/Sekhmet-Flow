package com.sekhmet.llmflow.service.engine.nodes;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.sekhmet.llmflow.config.LlmConfig;
import com.sekhmet.llmflow.model.dto.ModelConfig;
import com.sekhmet.llmflow.model.graph.NodeDefinition;
import com.sekhmet.llmflow.service.LlmService;
import com.sekhmet.llmflow.service.ModelPoolService;
import com.sekhmet.llmflow.service.engine.NodeExecutor;

import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;

/**
 * LLM 节点执行器
 * 调用 LLM 服务生成回复，支持从模型池获取模型或使用节点配置
 */
@Component
@RequiredArgsConstructor
public class LlmNodeExecutor implements NodeExecutor {

  private final LlmService llmService;
  private final ModelPoolService modelPoolService;

  /**
   * 执行 LLM 调用
   * 从输入中获取提示词，并应用节点特定的配置覆盖
   * 
   * @param node   节点定义 (包含模型配置)
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

    Map<String, Object> data = node.getData() != null ? node.getData() : new HashMap<>();

    dev.langchain4j.model.output.Response<dev.langchain4j.data.message.AiMessage> responseObj;
    ModelConfig usedConfig = null;

    // 1. 尝试从模型池获取 (优先)
    String modelId = (String) data.get("modelId");
    ChatLanguageModel pooledModel = null;
    if (modelId != null && !modelId.isEmpty()) {
      pooledModel = modelPoolService.getModel(modelId);
      if (pooledModel != null) {
        usedConfig = modelPoolService.getConfig(modelId);
      }
    }

    // 提取系统提示词
    String systemPrompt = "";
    if (inputs.containsKey("systemPrompt")) {
      systemPrompt = (String) inputs.get("systemPrompt");
    }

    if (pooledModel != null) {
      responseObj = llmService.generateWithModelAndSystemPrompt(systemPrompt, inputPrompt, pooledModel);
    } else {
      // 2. 如果没提供 modelId 或池中没找到，则使用节点自身的覆盖配置
      LlmConfig overrideConfig = extractOverrideConfig(data);
      // Convert LlmConfig to ModelConfig for consistency in logging (partial)
      usedConfig = ModelConfig.builder()
          .provider(overrideConfig.getProvider())
          .modelName(overrideConfig.getModelName())
          .temperature(overrideConfig.getTemperature())
          .topK(overrideConfig.getTopK())
          .topP(overrideConfig.getTopP())
          .build();

      responseObj = llmService.generateWithSystemPrompt(systemPrompt, inputPrompt, overrideConfig);
    }

    String responseText = responseObj.content().text();
    dev.langchain4j.model.output.TokenUsage tokenUsage = responseObj.tokenUsage();

    Map<String, Object> output = new HashMap<>();
    output.put("response", responseText);

    // Construct Debug Info
    Map<String, Object> debugInfo = new HashMap<>();
    if (usedConfig != null) {
      debugInfo.put("provider", usedConfig.getProvider());
      debugInfo.put("model", usedConfig.getModelName()); // Or ID?
      debugInfo.put("temperature", usedConfig.getTemperature());
      debugInfo.put("topK", usedConfig.getTopK());
      debugInfo.put("topP", usedConfig.getTopP());
    }

    if (tokenUsage != null) {
      Map<String, Object> usageMap = new HashMap<>();
      usageMap.put("input", tokenUsage.inputTokenCount());
      usageMap.put("output", tokenUsage.outputTokenCount());
      usageMap.put("total", tokenUsage.totalTokenCount());
      debugInfo.put("tokenUsage", usageMap);
    }

    output.put("_execution_config", debugInfo);

    return output;
  }

  private LlmConfig extractOverrideConfig(Map<String, Object> data) {
    String provider = (String) data.getOrDefault("provider", "openai");
    String modelName = (String) data.get("modelName");
    String apiKey = (String) data.get("apiKey");
    String baseUrl = (String) data.get("baseUrl");
    Object tempObj = data.get("temperature");
    Object topKObj = data.get("topK");
    Object topPObj = data.get("topP");

    LlmConfig overrideConfig = new LlmConfig();
    overrideConfig.setProvider(provider);
    if (modelName != null && !modelName.isEmpty())
      overrideConfig.setModelName(modelName);
    if (apiKey != null && !apiKey.isEmpty())
      overrideConfig.setApiKey(apiKey);
    if (baseUrl != null && !baseUrl.isEmpty())
      overrideConfig.setBaseUrl(baseUrl);
    if (tempObj != null) {
      try {
        overrideConfig.setTemperature(Double.parseDouble(tempObj.toString()));
      } catch (NumberFormatException ignored) {
      }
    }
    if (topKObj != null) {
      try {
        overrideConfig.setTopK(Integer.parseInt(topKObj.toString()));
      } catch (NumberFormatException ignored) {
      }
    }
    if (topPObj != null) {
      try {
        overrideConfig.setTopP(Double.parseDouble(topPObj.toString()));
      } catch (NumberFormatException ignored) {
      }
    }
    return overrideConfig;
  }

  @Override
  public String getNodeType() {
    return "llm-node";
  }
}
