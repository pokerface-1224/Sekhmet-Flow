package com.sekhmet.llmflow.service;

import org.springframework.stereotype.Service;

import com.sekhmet.llmflow.config.LlmConfig;
import com.sekhmet.llmflow.model.dto.ModelConfig;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * LLM 服务类
 * 封装 LangChain4j 的调用，支持多 Provider (OpenAI, Gemini, DeepSeek)
 */
@Service
@Slf4j
public class LlmService {

  private final LlmConfig globalConfig;
  private final ModelFactory modelFactory;
  private ChatModel defaultModel;

  public LlmService(LlmConfig llmConfig, ModelFactory modelFactory) {
    this.globalConfig = llmConfig;
    this.modelFactory = modelFactory;
    // 尝试初始化默认模型，如果失败则记录日志但不阻止应用启动
    try {
      this.defaultModel = createModelFromGlobalConfig(llmConfig);
    } catch (IllegalArgumentException e) {
      System.out.println("LlmService: Default model not initialized (" + e.getMessage() + ")");
      this.defaultModel = null;
    }
  }

  private ChatModel createModelFromGlobalConfig(LlmConfig config) {
    ModelConfig dto = ModelConfig.builder()
        .provider(config.getProvider())
        .apiKey(config.getApiKey())
        .baseUrl(config.getBaseUrl())
        .modelName(config.getModelName())
        .temperature(config.getTemperature())
        .topK(config.getTopK())
        .topP(config.getTopP())
        .build();
    return modelFactory.createModel(dto);
  }

  /**
   * 生成文本
   * 
   * @param prompt 提示词
   * @return 生成的文本
   */
  public String generate(String prompt) {
    return generate(prompt, null);
  }

  /**
   * 使用指定模型实例生成文本 (带详细信息)
   */
  public ChatResponse generateWithModelDetails(String prompt, ChatModel model) {
    if (model == null) {
      throw new RuntimeException("Model instance is null");
    }
    try {
      log.info("\n====== AI Prompt (User) ======\n{}\n=======================", prompt);
      List<ChatMessage> messages = new ArrayList<>();
      messages.add(UserMessage.from(prompt));
      ChatRequest request = ChatRequest.builder()
          .messages(messages)
          .build();
      return model.chat(request);
    } catch (Exception e) {
      throw new RuntimeException("Error calling LLM: " + e.getMessage(), e);
    }
  }

  /**
   * 使用指定模型实例生成文本 (带系统提示词)
   */
  public ChatResponse generateWithModelAndSystemPrompt(String systemPrompt, String userPrompt,
      ChatModel model) {
    if (model == null) {
      throw new RuntimeException("Model instance is null");
    }

    if (systemPrompt == null || systemPrompt.isEmpty()) {
      return generateWithModelDetails(userPrompt, model);
    }

    List<ChatMessage> messages = new ArrayList<>();
    messages.add(SystemMessage.from(systemPrompt));
    messages.add(UserMessage.from(userPrompt));

    try {
      log.info("\n====== AI Prompt ======\n[System]:\n{}\n\n[User]:\n{}\n=======================", systemPrompt, userPrompt);
      ChatRequest request = ChatRequest.builder()
          .messages(messages)
          .build();
      return model.chat(request);
    } catch (Exception e) {
      throw new RuntimeException("Error calling LLM: " + e.getMessage(), e);
    }
  }

  /**
   * 使用指定模型实例生成文本
   */
  public String generateWithModel(String prompt, ChatModel model) {
    return generateWithModelDetails(prompt, model).aiMessage().text();
  }

  /**
   * 生成文本 (支持覆盖配置)
   * 
   * @param prompt         提示词
   * @param overrideConfig 覆盖的配置
   * @return 生成的文本
   */
  public String generate(String prompt, LlmConfig overrideConfig) {
    ChatResponse response = generateWithDetails(prompt, overrideConfig);
    return response.aiMessage().text();
  }

  /**
   * 生成文本 (支持覆盖配置) - 返回详细信息
   */
  public ChatResponse generateWithDetails(String prompt, LlmConfig overrideConfig) {
    ChatModel modelToUse = getModelForConfig(overrideConfig);

    if (modelToUse == null) {
      LlmConfig configToUse = (overrideConfig != null) ? overrideConfig : globalConfig;
      AiMessage mockMessage = AiMessage.from("Mock LLM Response (" + configToUse.getProvider() + ") for prompt: " + prompt);
      return ChatResponse.builder()
          .aiMessage(mockMessage)
          .build();
    }

    try {
      log.info("\n====== AI Prompt (User) ======\n{}\n=======================", prompt);
      List<ChatMessage> messages = new ArrayList<>();
      messages.add(UserMessage.from(prompt));
      ChatRequest request = ChatRequest.builder()
          .messages(messages)
          .build();
      return modelToUse.chat(request);
    } catch (Exception e) {
      LlmConfig configToUse = (overrideConfig != null) ? overrideConfig : globalConfig;
      throw new RuntimeException("Error calling LLM (" + configToUse.getProvider() + "): " + e.getMessage(), e);
    }
  }

  /**
   * 生成文本 (支持系统提示词和覆盖配置)
   */
  public ChatResponse generateWithSystemPrompt(String systemPrompt, String userPrompt,
      LlmConfig overrideConfig) {
    if (systemPrompt == null || systemPrompt.isEmpty()) {
      return generateWithDetails(userPrompt, overrideConfig);
    }

    ChatModel modelToUse = getModelForConfig(overrideConfig);

    List<ChatMessage> messages = new ArrayList<>();
    messages.add(SystemMessage.from(systemPrompt));
    messages.add(UserMessage.from(userPrompt));

    try {
      log.info("\n====== AI Prompt ======\n[System]:\n{}\n\n[User]:\n{}\n=======================", systemPrompt, userPrompt);
      ChatRequest request = ChatRequest.builder()
          .messages(messages)
          .build();
      return modelToUse.chat(request);
    } catch (Exception e) {
      throw new RuntimeException("Error calling LLM with System Prompt: " + e.getMessage(), e);
    }
  }

  private ChatModel getModelForConfig(LlmConfig overrideConfig) {
    LlmConfig configToUse = this.globalConfig;
    ChatModel modelToUse = null;

    if (overrideConfig != null) {
      LlmConfig mergedConfig = new LlmConfig();

      String effectiveProvider = overrideConfig.getProvider() != null && !overrideConfig.getProvider().isEmpty()
          ? overrideConfig.getProvider()
          : globalConfig.getProvider();
      mergedConfig.setProvider(effectiveProvider);

      String effectiveKey = null;
      if (overrideConfig.getApiKey() != null && !overrideConfig.getApiKey().isEmpty()) {
        effectiveKey = overrideConfig.getApiKey();
      } else if (globalConfig.getProvider().equalsIgnoreCase(effectiveProvider)) {
        effectiveKey = globalConfig.getApiKey();
      }
      mergedConfig.setApiKey(effectiveKey);

      mergedConfig.setBaseUrl(
          overrideConfig.getBaseUrl() != null ? overrideConfig.getBaseUrl() : globalConfig.getBaseUrl());
      mergedConfig.setModelName(overrideConfig.getModelName() != null ? overrideConfig.getModelName()
          : globalConfig.getModelName());
      mergedConfig.setTemperature(overrideConfig.getTemperature() != null ? overrideConfig.getTemperature()
          : globalConfig.getTemperature());
      mergedConfig.setTopK(overrideConfig.getTopK() != null ? overrideConfig.getTopK()
          : globalConfig.getTopK());
      mergedConfig.setTopP(overrideConfig.getTopP() != null ? overrideConfig.getTopP()
          : globalConfig.getTopP());

      configToUse = mergedConfig;
      modelToUse = createModelFromGlobalConfig(mergedConfig);
    } else {
      modelToUse = this.defaultModel;
    }

    if (modelToUse == null) {
      if ("demo".equals(configToUse.getApiKey())) {
        return null;
      }
      throw new RuntimeException(
          "LLM Provider (" + configToUse.getProvider() + ") not initialized. Check API Key.");
    }
    return modelToUse;
  }
}
