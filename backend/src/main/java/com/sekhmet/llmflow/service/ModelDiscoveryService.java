package com.sekhmet.llmflow.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * 模型发现服务
 * 从各 LLM 提供商 API 获取可用模型列表
 */
@Service
@Slf4j
public class ModelDiscoveryService {

    private final RestTemplate restTemplate;

    public ModelDiscoveryService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * 发现指定提供商的可用模型
     * 
     * @param provider 提供商 (openai, gemini, deepseek)
     * @param apiKey   API 密钥
     * @param baseUrl  可选的自定义 Base URL
     * @return 可用模型名称列表
     */
    public List<String> discoverModels(String provider, String apiKey, String baseUrl) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalArgumentException("API Key is required for model discovery");
        }

        String normalizedProvider = provider != null ? provider.toLowerCase() : "openai";

        switch (normalizedProvider) {
            case "gemini":
                return discoverGeminiModels(apiKey);
            case "deepseek":
                return discoverOpenAiCompatibleModels(apiKey,
                        baseUrl != null && !baseUrl.isEmpty() ? baseUrl : "https://api.deepseek.com");
            case "openai":
            default:
                return discoverOpenAiCompatibleModels(apiKey,
                        baseUrl != null && !baseUrl.isEmpty() ? baseUrl : "https://api.openai.com");
        }
    }

    /**
     * 发现 OpenAI 兼容 API 的模型 (OpenAI, DeepSeek 等)
     */
    @SuppressWarnings("unchecked")
    private List<String> discoverOpenAiCompatibleModels(String apiKey, String baseUrl) {
        String url = baseUrl.endsWith("/") ? baseUrl + "v1/models" : baseUrl + "/v1/models";
        log.info("Discovering models from: {}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<String, Object> body = response.getBody();

            if (body == null || !body.containsKey("data")) {
                log.warn("No 'data' field in response from {}", url);
                return new ArrayList<>();
            }

            List<Map<String, Object>> data = (List<Map<String, Object>>) body.get("data");
            List<String> modelIds = new ArrayList<>();

            for (Map<String, Object> model : data) {
                String id = (String) model.get("id");
                if (id != null) {
                    modelIds.add(id);
                }
            }

            log.info("Discovered {} models from {}", modelIds.size(), baseUrl);
            return modelIds;

        } catch (RestClientException e) {
            log.error("Failed to discover models from {}: {}", url, e.getMessage());
            throw new RuntimeException("Failed to discover models: " + e.getMessage(), e);
        }
    }

    /**
     * 发现 Google Gemini 模型
     */
    @SuppressWarnings("unchecked")
    private List<String> discoverGeminiModels(String apiKey) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models?key=" + apiKey;
        log.info("Discovering Gemini models");

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();

            if (body == null || !body.containsKey("models")) {
                log.warn("No 'models' field in Gemini response");
                return new ArrayList<>();
            }

            List<Map<String, Object>> models = (List<Map<String, Object>>) body.get("models");
            List<String> modelNames = new ArrayList<>();

            for (Map<String, Object> model : models) {
                String name = (String) model.get("name");
                if (name != null) {
                    // Gemini API 返回的是 "models/gemini-1.5-flash"，我们只取后半部分
                    String modelId = name.startsWith("models/") ? name.substring(7) : name;
                    modelNames.add(modelId);
                }
            }

            log.info("Discovered {} Gemini models", modelNames.size());
            return modelNames;

        } catch (RestClientException e) {
            log.error("Failed to discover Gemini models: {}", e.getMessage());
            throw new RuntimeException("Failed to discover Gemini models: " + e.getMessage(), e);
        }
    }
}
