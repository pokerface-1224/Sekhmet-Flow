package com.sekhmet.llmflow.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * LLM 服务配置属性类
 * 映射 application.yml 中 sekhmet.llm 开头的配置项
 */
@Configuration
@ConfigurationProperties(prefix = "sekhmet.llm")
@Data
public class LlmConfig {
    /** LLM 提供商 (openai, gemini, deepseek) */
    private String provider = "openai";
    /** API 密钥 */
    private String apiKey;
    /** API 基础地址 */
    private String baseUrl;
    /** 模型名称 */
    private String modelName;
    /** 温度参数 (0.0 - 2.0)，控制生成结果的随机性 */
    private Double temperature = 0.7;
}
