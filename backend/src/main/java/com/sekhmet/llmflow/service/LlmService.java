package com.sekhmet.llmflow.service;

import org.springframework.stereotype.Service;

import com.sekhmet.llmflow.config.LlmConfig;
import com.sekhmet.llmflow.model.dto.ModelConfig;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.output.Response;
import lombok.extern.slf4j.Slf4j;

/**
 * LLM 服务类
 * 封装 LangChain4j 的调用，支持多 Provider (OpenAI, Gemini, DeepSeek)
 */
@Service
@Slf4j
public class LlmService {

  private final LlmConfig globalConfig;
  private final ModelFactory modelFactory;
  private ChatLanguageModel defaultModel;

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

  private ChatLanguageModel createModelFromGlobalConfig(LlmConfig config) {
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
  public Response<AiMessage> generateWithModelDetails(String prompt, ChatLanguageModel model) {
    if (model == null) {
      throw new RuntimeException("Model instance is null");
    }
    try {
      log.info("\n====== AI Prompt (User) ======\n{}\n=======================", prompt);
      return model.generate(UserMessage.from(prompt));
    } catch (Exception e) {
      throw new RuntimeException("Error calling LLM: " + e.getMessage(), e);
    }
  }

  /**
   * 使用指定模型实例生成文本 (带系统提示词)
   */
  public Response<AiMessage> generateWithModelAndSystemPrompt(String systemPrompt, String userPrompt,
      ChatLanguageModel model) {
    if (model == null) {
      throw new RuntimeException("Model instance is null");
    }

    if (systemPrompt == null || systemPrompt.isEmpty()) {
      return generateWithModelDetails(userPrompt, model);
    }

    java.util.List<dev.langchain4j.data.message.ChatMessage> messages = new java.util.ArrayList<>();
    messages.add(dev.langchain4j.data.message.SystemMessage.from(systemPrompt));
    messages.add(UserMessage.from(userPrompt));

    try {
      log.info("\n====== AI Prompt ======\n[System]:\n{}\n\n[User]:\n{}\n=======================", systemPrompt, userPrompt);
      return model.generate(messages);
    } catch (Exception e) {
      throw new RuntimeException("Error calling LLM: " + e.getMessage(), e);
    }
  }

  /**
   * 使用指定模型实例生成文本
   */
  public String generateWithModel(String prompt, ChatLanguageModel model) {
    return generateWithModelDetails(prompt, model).content().text();
  }

  /**
   * 生成文本 (支持覆盖配置)
   * 
   * @param prompt         提示词
   * @param overrideConfig 覆盖的配置
   * @return 生成的文本
   */
  public String generate(String prompt, LlmConfig overrideConfig) {
    Response<AiMessage> response = generateWithDetails(prompt, overrideConfig);
    return response.content().text();
  }

  /**
   * 生成文本 (支持覆盖配置) - 返回详细信息
   */
  /**
   * 生成文本 (支持覆盖配置) - 返回详细信息
   */
  public Response<AiMessage> generateWithDetails(String prompt, LlmConfig overrideConfig) {
    // Logic refactored to use helper
    ChatLanguageModel modelToUse = getModelForConfig(overrideConfig);

    if (modelToUse == null) {
      // It means it was a demo config
      LlmConfig configToUse = (overrideConfig != null) ? overrideConfig : globalConfig; // Simplified
      return Response
          .from(AiMessage.from("Mock LLM Response (" + configToUse.getProvider() + ") for prompt: " + prompt));
    }

    try {
      log.info("\n====== AI Prompt (User) ======\n{}\n=======================", prompt);
      return modelToUse.generate(UserMessage.from(prompt));
    } catch (Exception e) {
      LlmConfig configToUse = (overrideConfig != null) ? overrideConfig : globalConfig;
      throw new RuntimeException("Error calling LLM (" + configToUse.getProvider() + "): " + e.getMessage(), e);
    }
  }

  /**
   * 生成文本 (支持系统提示词和覆盖配置)
   */
  public Response<AiMessage> generateWithSystemPrompt(String systemPrompt, String userPrompt,
      LlmConfig overrideConfig) {
    if (systemPrompt == null || systemPrompt.isEmpty()) {
      return generateWithDetails(userPrompt, overrideConfig);
    }

    // Logic similar to generateWithDetails to get the model,
    // but we need to construct a list of messages.
    // Ideally, we should refactor getModel logic, but for now we duplicate or
    // reuse.
    // Let's reuse the logic by extracting the model retrieval.

    ChatLanguageModel modelToUse = getModelForConfig(overrideConfig);

    java.util.List<dev.langchain4j.data.message.ChatMessage> messages = new java.util.ArrayList<>();
    messages.add(dev.langchain4j.data.message.SystemMessage.from(systemPrompt));
    messages.add(UserMessage.from(userPrompt));

    try {
      log.info("\n====== AI Prompt ======\n[System]:\n{}\n\n[User]:\n{}\n=======================", systemPrompt, userPrompt);
      return modelToUse.generate(messages);
    } catch (Exception e) {
      throw new RuntimeException("Error calling LLM with System Prompt: " + e.getMessage(), e);
    }
  }

  private ChatLanguageModel getModelForConfig(LlmConfig overrideConfig) {
    LlmConfig configToUse = this.globalConfig;
    ChatLanguageModel modelToUse = null;

    // If override provided, create a temporary model instance
    if (overrideConfig != null) {
      LlmConfig mergedConfig = new LlmConfig();

      // 1. Determine Provider
      String effectiveProvider = overrideConfig.getProvider() != null && !overrideConfig.getProvider().isEmpty()
          ? overrideConfig.getProvider()
          : globalConfig.getProvider();
      mergedConfig.setProvider(effectiveProvider);

      // 2. Determine API Key (No cross-provider inheritance)
      String effectiveKey = null;
      if (overrideConfig.getApiKey() != null && !overrideConfig.getApiKey().isEmpty()) {
        effectiveKey = overrideConfig.getApiKey();
      } else if (globalConfig.getProvider().equalsIgnoreCase(effectiveProvider)) {
        // Only inherit global key if providers match
        effectiveKey = globalConfig.getApiKey();
      }
      mergedConfig.setApiKey(effectiveKey);

      // 3. Other fields
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
      // Throw exception if creation fails
      modelToUse = createModelFromGlobalConfig(mergedConfig);
    } else {
      modelToUse = this.defaultModel;
    }

    if (modelToUse == null) {
      // Null model implies demo/mock mode due to "demo" key check in createModel
      if ("demo".equals(configToUse.getApiKey())) {
        return null; // Handle mock in caller if needed, or implement a MockChatModel
      }
      throw new RuntimeException(
          "LLM Provider (" + configToUse.getProvider() + ") not initialized. Check API Key.");
    }
    return modelToUse;
  }
}
