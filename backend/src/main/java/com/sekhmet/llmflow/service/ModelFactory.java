package com.sekhmet.llmflow.service;

import org.springframework.stereotype.Component;

import com.sekhmet.llmflow.model.dto.ModelConfig;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

/**
 * 模型工厂类
 * 负责根据配置创建 ChatLanguageModel 实例
 */
@Component
public class ModelFactory {

  public ChatLanguageModel createModel(ModelConfig config) {
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

    switch (provider) {
      case "gemini":
        return GoogleAiGeminiChatModel.builder()
            .apiKey(apiKey)
            .modelName(config.getModelName() != null ? config.getModelName() : "gemini-1.5-flash")
            .temperature(temperature)
            .build();
      case "deepseek":
        return OpenAiChatModel.builder()
            .apiKey(apiKey)
            .baseUrl(config.getBaseUrl() != null && !config.getBaseUrl().isEmpty()
                ? config.getBaseUrl()
                : "https://api.deepseek.com")
            .modelName(config.getModelName() != null ? config.getModelName() : "deepseek-chat")
            .temperature(temperature)
            .build();
      case "openai":
      default:
        OpenAiChatModel.OpenAiChatModelBuilder builder = OpenAiChatModel.builder()
            .apiKey(apiKey)
            .modelName(config.getModelName() != null ? config.getModelName() : "gpt-3.5-turbo")
            .temperature(temperature);

        if (config.getBaseUrl() != null && !config.getBaseUrl().isEmpty()) {
          builder.baseUrl(config.getBaseUrl());
        }
        return builder.build();
    }
  }
}
