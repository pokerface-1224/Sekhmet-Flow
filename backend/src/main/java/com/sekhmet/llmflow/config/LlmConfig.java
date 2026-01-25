package com.sekhmet.llmflow.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "sekhmet.llm")
@Data
public class LlmConfig {
    private String provider = "openai"; // openai, gemini, deepseek
    private String apiKey;
    private String baseUrl;
    private String modelName;
    private Double temperature = 0.7;
}
