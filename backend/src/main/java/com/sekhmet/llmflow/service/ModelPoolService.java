package com.sekhmet.llmflow.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sekhmet.llmflow.model.dto.ModelConfig;
import com.sekhmet.llmflow.model.entity.ModelConfigEntity;
import com.sekhmet.llmflow.repository.ModelConfigJpaRepository;

import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 模型池服务
 * 使用 SQLite 持久化模型配置，内存缓存模型实例
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ModelPoolService {

  private final ModelFactory modelFactory;
  private final ModelConfigJpaRepository configRepository;

  // 内存缓存模型实例: id -> ChatLanguageModel
  private final Map<String, ChatLanguageModel> modelCache = new ConcurrentHashMap<>();

  /**
   * 注册或更新模型配置 (持久化到 SQLite)
   */
  public void upsertModel(ModelConfig config) {
    if (config.getId() == null || config.getId().isEmpty()) {
      throw new IllegalArgumentException("Model ID cannot be empty");
    }

    log.info("Upserting model config: {}", config.getId());

    // 转换为实体并保存到 SQLite
    ModelConfigEntity entity = toEntity(config);
    configRepository.save(entity);

    // 清除内存缓存，下次获取时会按新配置创建
    modelCache.remove(config.getId());
  }

  /**
   * 获取模型实例 (懒加载 + 内存缓存)
   */
  public ChatLanguageModel getModel(String id) {
    // 先检查 SQLite 中是否存在配置
    ModelConfigEntity entity = configRepository.findById(id).orElse(null);
    if (entity == null) {
      return null;
    }

    // 使用 computeIfAbsent 保证原子性
    return modelCache.computeIfAbsent(id, key -> {
      log.info("Creating new model instance for ID: {}", key);
      return modelFactory.createModel(toDto(entity));
    });
  }

  /**
   * 获取模型配置 (从 SQLite 读取)
   */
  public ModelConfig getConfig(String id) {
    return configRepository.findById(id)
        .map(this::toDto)
        .orElse(null);
  }

  /**
   * 移除模型 (从 SQLite 和内存缓存中删除)
   */
  public void removeModel(String id) {
    log.info("Removing model from pool: {}", id);
    configRepository.deleteById(id);
    modelCache.remove(id);
  }

  /**
   * 获取所有配置 (从 SQLite 读取)
   */
  public List<ModelConfig> getAllConfigs() {
    return configRepository.findAll().stream()
        .map(this::toDto)
        .collect(Collectors.toList());
  }

  /**
   * 获取所有配置 (apiKey 掩码化，用于前端展示)
   */
  public List<ModelConfig> getAllConfigsMasked() {
    return configRepository.findAll().stream()
        .map(entity -> {
          ModelConfig dto = toDto(entity);
          dto.setApiKey(maskApiKey(dto.getApiKey()));
          return dto;
        })
        .collect(Collectors.toList());
  }

  // ========== 转换方法 ==========

  private ModelConfigEntity toEntity(ModelConfig dto) {
    return ModelConfigEntity.builder()
        .id(dto.getId())
        .provider(dto.getProvider())
        .apiKey(dto.getApiKey())
        .baseUrl(dto.getBaseUrl())
        .modelName(dto.getModelName())
        .temperature(dto.getTemperature())
        .topK(dto.getTopK())
        .topP(dto.getTopP())
        .build();
  }

  private ModelConfig toDto(ModelConfigEntity entity) {
    return ModelConfig.builder()
        .id(entity.getId())
        .provider(entity.getProvider())
        .apiKey(entity.getApiKey())
        .baseUrl(entity.getBaseUrl())
        .modelName(entity.getModelName())
        .temperature(entity.getTemperature())
        .topK(entity.getTopK())
        .topP(entity.getTopP())
        .build();
  }

  /**
   * 掩码 API Key (保留前3位和后4位)
   */
  private String maskApiKey(String apiKey) {
    if (apiKey == null || apiKey.length() <= 7) {
      return "****";
    }
    return apiKey.substring(0, 3) + "****" + apiKey.substring(apiKey.length() - 4);
  }
}
