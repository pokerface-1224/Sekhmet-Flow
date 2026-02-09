package com.sekhmet.llmflow.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模型配置 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelConfig {
  /** 唯一标识符 (例如 "openai-gpt-4") */
  private String id;
  /** 显示名称 */
  private String name;
  /** 提供商 (openai, gemini, deepseek, ollama 等) */
  private String provider;
  /** API 密钥 */
  private String apiKey;
  /** API 基础地址 */
  private String baseUrl;
  /** 模型名称 */
  private String modelName;
  /** 温度参数 */
  private Double temperature;
  /** Top K 参数 */
  private Integer topK;
  /** Top P 参数 */
  private Double topP;
}
