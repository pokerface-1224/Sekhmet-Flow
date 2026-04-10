package com.sekhmet.llmflow.service;

import org.springframework.stereotype.Component;

import com.sekhmet.llmflow.model.dto.ModelConfig;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

/**
 * 模型工厂类
 * 负责根据配置创建 ChatLanguageModel 实例
 */
@Component
public class ModelFactory {

  public ChatModel createModel(ModelConfig config) {
    String apiKey = config.getApiKey();

    // 显式 "demo" 模式
    if ("demo".equals(apiKey)) {
      return null;
    }

    String provider = config.getProvider() != null ? config.getProvider().toLowerCase() : "openai";

    // 检查 API Key
    if (apiKey == null || apiKey.isEmpty()) {
      throw new IllegalArgumentException("API Key is missing for provider: " + provider);
    }

    Double temperature = config.getTemperature() != null ? config.getTemperature() : 0.7;

    // 判断是否为推理模型（需要启用思维链）
    String modelName = config.getModelName() != null ? config.getModelName() : "";
    boolean isReasoningModel = modelName.contains("reasoner") || modelName.contains("thinking");

    switch (provider) {
      case "gemini":
        return GoogleAiGeminiChatModel.builder()
            .apiKey(apiKey)
            .modelName(modelName.isEmpty() ? "gemini-1.5-flash" : modelName)
            .temperature(temperature)
            .build();
      case "deepseek": {
        OpenAiChatModel.OpenAiChatModelBuilder builder = OpenAiChatModel.builder()
            .apiKey(apiKey)
            .baseUrl(config.getBaseUrl() != null && !config.getBaseUrl().isEmpty()
                ? config.getBaseUrl()
                : "https://api.deepseek.com")
            .modelName(modelName.isEmpty() ? "deepseek-chat" : modelName)
            .temperature(temperature);

        // 推理模型启用思维链输出
        if (isReasoningModel) {
          builder.returnThinking(true);
        }

        return builder.build();
      }
      case "openai兼容":
      case "openai":
      default: {
        OpenAiChatModel.OpenAiChatModelBuilder builder = OpenAiChatModel.builder()
            .apiKey(apiKey)
            .modelName(modelName.isEmpty() ? "gpt-3.5-turbo" : modelName)
            .temperature(temperature);

        if (config.getBaseUrl() != null && !config.getBaseUrl().isEmpty()) {
          builder.baseUrl(config.getBaseUrl());
        }

        // 推理模型启用思维链输出
        if (isReasoningModel) {
          builder.returnThinking(true);
        }

        return builder.build();
      }
    }
  }
}
