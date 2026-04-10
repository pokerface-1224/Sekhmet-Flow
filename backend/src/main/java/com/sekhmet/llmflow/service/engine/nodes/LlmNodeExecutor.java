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

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.output.TokenUsage;
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
   * @return 包含 response、thinkingContent 和 text 的结果映射
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

    ChatResponse responseObj;
    ModelConfig usedConfig = null;

    // 1. 尝试从模型池获取 (优先)
    String modelId = (String) data.get("modelId");
    ChatModel pooledModel = null;
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

    AiMessage aiMessage = responseObj.aiMessage();
    String responseText = aiMessage.text();

    Map<String, Object> output = new HashMap<>();
    output.put("response", responseText);

    // 提取思维链内容 (reasoning_content)
    String thinkingContent = aiMessage.thinking();
    if (thinkingContent != null && !thinkingContent.isEmpty()) {
      output.put("thinkingContent", thinkingContent);
    }

    // Construct Debug Info
    Map<String, Object> debugInfo = new HashMap<>();
    if (usedConfig != null) {
      debugInfo.put("provider", usedConfig.getProvider());
      debugInfo.put("model", usedConfig.getModelName());
      debugInfo.put("temperature", usedConfig.getTemperature());
      debugInfo.put("topK", usedConfig.getTopK());
      debugInfo.put("topP", usedConfig.getTopP());
    }

    TokenUsage tokenUsage = responseObj.tokenUsage();
    if (tokenUsage != null) {
      Map<String, Object> usageMap = new HashMap<>();
      usageMap.put("input", tokenUsage.inputTokenCount());
      usageMap.put("output", tokenUsage.outputTokenCount());
      usageMap.put("total", tokenUsage.totalTokenCount());
      debugInfo.put("tokenUsage", usageMap);
    }

    if (systemPrompt != null && !systemPrompt.isEmpty()) {
      debugInfo.put("finalPromptLayout", "[System]:\n" + systemPrompt + "\n\n[User]:\n" + inputPrompt);
    } else {
      debugInfo.put("finalPromptLayout", "[User]:\n" + inputPrompt);
    }

    // 如果有思维链，也记录到 debug 信息中
    if (thinkingContent != null && !thinkingContent.isEmpty()) {
      debugInfo.put("hasThinking", true);
      debugInfo.put("thinkingPreview", thinkingContent.length() > 500
          ? thinkingContent.substring(0, 500) + "..."
          : thinkingContent);
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
