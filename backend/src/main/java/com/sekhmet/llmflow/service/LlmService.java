package com.sekhmet.llmflow.service;

import com.sekhmet.llmflow.config.LlmConfig;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;

@Service
public class LlmService {

    private final LlmConfig globalConfig;
    private ChatLanguageModel defaultModel;

    public LlmService(LlmConfig llmConfig) {
        this.globalConfig = llmConfig;
        // Don't swallow exceptions here, let startup fail or handle lazily?
        // Current logic tried to swallow and set null, but we want to fail fast or at
        // least fail loudly on execution.
        // Keeping it simple: try to init, if fail, log but allow app to start.
        // Accessing it later will fail.
        try {
            this.defaultModel = createModel(llmConfig);
        } catch (IllegalArgumentException e) {
            System.out.println("LlmService: Default model not initialized (" + e.getMessage() + ")");
            this.defaultModel = null;
        }
    }

    private ChatLanguageModel createModel(LlmConfig config) {
        String apiKey = config.getApiKey();

        // Explicit "demo" mode
        if ("demo".equals(apiKey)) {
            return null;
        }

        String provider = config.getProvider() != null ? config.getProvider().toLowerCase() : "openai";

        // Missing key check
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
                        .apiKey(apiKey) // Deepseek uses its own key
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

    public String generate(String prompt) {
        return generate(prompt, null);
    }

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
