package com.sekhmet.llmflow.service;

import com.sekhmet.llmflow.config.LlmConfig;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;

/**
 * LLM 服务类
 * 封装 LangChain4j 的调用，支持多 Provider (OpenAI, Gemini, DeepSeek)
 */
@Service
public class LlmService {

    private final LlmConfig globalConfig;
    private ChatLanguageModel defaultModel;

    public LlmService(LlmConfig llmConfig) {
        this.globalConfig = llmConfig;
        // 尝试初始化默认模型，如果失败则记录日志但不阻止应用启动
        try {
            this.defaultModel = createModel(llmConfig);
        } catch (IllegalArgumentException e) {
            System.out.println("LlmService: Default model not initialized (" + e.getMessage() + ")");
            this.defaultModel = null;
        }
    }

    /**
     * 根据配置创建 ChatLanguageModel 实例
     * @param config LLM 配置
     * @return ChatLanguageModel 实例
     */
    private ChatLanguageModel createModel(LlmConfig config) {
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
                        .apiKey(apiKey) // Deepseek 使用自己的 key
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

    /**
     * 生成文本
     * @param prompt 提示词
     * @return 生成的文本
     */
    public String generate(String prompt) {
        return generate(prompt, null);
    }

    /**
     * 生成文本 (支持覆盖配置)
     * @param prompt 提示词
     * @param overrideConfig 覆盖的配置
     * @return 生成的文本
     */
    public String generate(String prompt, LlmConfig overrideConfig) {
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

            configToUse = mergedConfig;
            // Throw exception if creation fails
            modelToUse = createModel(mergedConfig);
        } else {
            modelToUse = this.defaultModel;
        }

        if (modelToUse == null) {
            // Null model implies demo/mock mode due to "demo" key check in createModel
            if ("demo".equals(configToUse.getApiKey())) {
                return "Mock LLM Response (" + configToUse.getProvider() + ") for prompt: " + prompt;
            }
            throw new RuntimeException(
                    "LLM Provider (" + configToUse.getProvider() + ") not initialized. Check API Key.");
        }

        try {
            return modelToUse.generate(prompt);
        } catch (Exception e) {
            throw new RuntimeException("Error calling LLM (" + configToUse.getProvider() + "): " + e.getMessage(), e);
        }
    }
}
