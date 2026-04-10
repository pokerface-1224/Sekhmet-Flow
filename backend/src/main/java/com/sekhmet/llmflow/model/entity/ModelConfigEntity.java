package com.sekhmet.llmflow.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模型配置 JPA 实体
 * 持久化到 SQLite 的 model_configs 表
 * 存储各 Provider 的 API Key 及模型参数
 */
@Entity
@Table(name = "model_configs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelConfigEntity {
    /** 唯一标识符 (如 "deepseek-deepseek-chat") */
    @Id
    private String id;
    /** 提供商 (openai兼容, gemini, deepseek) */
    private String provider;
    /** API 密钥 (明文存储) */
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
