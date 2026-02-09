package com.sekhmet.llmflow.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.sekhmet.llmflow.model.dto.ModelConfig;

import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 模型池服务
 * 管理内存中的模型配置和实例，支持热切换
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ModelPoolService {

  private final ModelFactory modelFactory;

  // 存储模型配置: id -> ModelConfig
  private final Map<String, ModelConfig> configMap = new ConcurrentHashMap<>();

  // 存储模型实例: id -> ChatLanguageModel
  private final Map<String, ChatLanguageModel> modelMap = new ConcurrentHashMap<>();

  /**
   * 注册或更新模型配置 (热切换)
   */
  public void upsertModel(ModelConfig config) {
    if (config.getId() == null || config.getId().isEmpty()) {
      throw new IllegalArgumentException("Model ID cannot be empty");
    }

    log.info("Upserting model config to pool: {}", config.getId());
    configMap.put(config.getId(), config);

    // 热切换：移除旧实例，下次获取时会按新配置创建
    modelMap.remove(config.getId());
  }

  /**
   * 获取模型实例 (懒加载，线程安全)
   */
  public ChatLanguageModel getModel(String id) {
    // 先检查配置是否存在
    ModelConfig config = configMap.get(id);
    if (config == null) {
      return null;
    }

    // 使用 computeIfAbsent 保证原子性：只有在不存在时才执行创建
    return modelMap.computeIfAbsent(id, key -> {
      log.info("Creating new model instance for ID: {}", key);
      return modelFactory.createModel(config);
    });
  }

  /**
   * 获取模型配置
   */
  public ModelConfig getConfig(String id) {
    return configMap.get(id);
  }

  /**
   * 移除模型
   */
  public void removeModel(String id) {
    log.info("Removing model from pool: {}", id);
    configMap.remove(id);
    modelMap.remove(id);
  }

  /**
   * 获取所有配置
   */
  public List<ModelConfig> getAllConfigs() {
    return new ArrayList<>(configMap.values());
  }
}
