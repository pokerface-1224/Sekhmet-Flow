package com.sekhmet.llmflow.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sekhmet.llmflow.config.LlmConfig;
import com.sekhmet.llmflow.model.dto.ModelConfig;
import com.sekhmet.llmflow.service.ModelDiscoveryService;
import com.sekhmet.llmflow.service.ModelPoolService;

import lombok.RequiredArgsConstructor;

/**
 * 模型管理控制器
 */
@RestController
@RequestMapping("/api/models")
@RequiredArgsConstructor
@CrossOrigin
public class ModelController {

  private final ModelPoolService modelPoolService;
  private final ModelDiscoveryService modelDiscoveryService;
  private final LlmConfig llmConfig;

  /**
   * 获取所有已配置的模型 (apiKey 掩码化)
   */
  @GetMapping
  public List<ModelConfig> listModels() {
    return modelPoolService.getAllConfigsMasked();
  }

  /**
   * 注册或更新模型 (支持热切换)
   */
  @PostMapping
  public void upsertModel(@RequestBody ModelConfig config) {
    modelPoolService.upsertModel(config);
  }

  /**
   * 移除模型
   */
  @DeleteMapping("/{id}")
  public void deleteModel(@PathVariable String id) {
    modelPoolService.removeModel(id);
  }

  /**
   * 发现指定提供商的可用模型
   * 
   * @param provider 提供商 (openai, gemini, deepseek)
   * @param apiKey   API 密钥 (可选，未传时使用全局配置)
   * @param baseUrl  可选的自定义 Base URL
   * @return 可用模型名称列表
   */
  @GetMapping("/discover")
  public List<String> discoverModels(
      @RequestParam(defaultValue = "openai") String provider,
      @RequestParam(required = false) String apiKey,
      @RequestParam(required = false) String baseUrl) {
    // 优先使用前端传入的 apiKey，否则回退到全局配置
    String effectiveKey = (apiKey != null && !apiKey.isEmpty()) ? apiKey : llmConfig.getApiKey();
    return modelDiscoveryService.discoverModels(provider, effectiveKey, baseUrl);
  }
}
