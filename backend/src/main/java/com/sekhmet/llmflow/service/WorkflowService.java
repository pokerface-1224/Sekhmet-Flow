package com.sekhmet.llmflow.service;

import com.sekhmet.llmflow.model.dto.ModelConfig;
import com.sekhmet.llmflow.model.entity.Workflow;
import com.sekhmet.llmflow.model.graph.NodeDefinition;
import com.sekhmet.llmflow.repository.JsonWorkflowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 工作流业务逻辑服务类
 * 处理工作流的存储和检索
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowService {
    private final JsonWorkflowRepository workflowRepository;
    private final ModelPoolService modelPoolService;

    /**
     * 获取所有工作流
     * @return 工作流列表
     */
    public List<Workflow> getAllWorkflows() {
        return workflowRepository.findAll();
    }

    /**
     * 保存工作流
     * 如果 ID 为空，则生成新的 ID
     * 保存前会自动将 LLM 节点的凭据持久化到 SQLite，然后从工作流中移除
     * @param workflow 工作流对象
     * @return 保存后的工作流
     */
    public Workflow saveWorkflow(Workflow workflow) {
        if (workflow.getId() == null || workflow.getId().isEmpty()) {
            workflow.setId(UUID.randomUUID().toString());
        }
        // 1. 先将节点中的模型凭据持久化到 SQLite
        persistModelCredentials(workflow);
        // 2. 然后从工作流中移除敏感信息
        sanitizeWorkflow(workflow);
        workflowRepository.save(workflow);
        return workflow;
    }

    /**
     * 根据 ID 获取工作流
     * @param id 工作流 ID
     * @return 工作流对象，如果不存在则返回 null
     */
    public Workflow getWorkflow(String id) {
        return workflowRepository.findById(id);
    }

    /**
     * 删除工作流
     * @param id 工作流 ID
     */
    public void deleteWorkflow(String id) {
        workflowRepository.deleteById(id);
    }

    /**
     * 将 LLM 节点中的模型凭据自动持久化到 SQLite
     * 确保无论用户是通过"发现"还是手动输入，apiKey 都不会丢失
     */
    private void persistModelCredentials(Workflow workflow) {
        if (workflow.getNodes() == null) return;

        for (NodeDefinition node : workflow.getNodes()) {
            if (!"llm-node".equals(node.getType())) continue;

            Map<String, Object> data = node.getData();
            if (data == null) continue;

            String apiKey = (String) data.get("apiKey");
            if (apiKey == null || apiKey.isEmpty()) continue;

            // 有 apiKey，需要持久化
            String provider = (String) data.getOrDefault("provider", "openai");
            String modelName = (String) data.get("modelName");
            String baseUrl = (String) data.get("baseUrl");

            // 生成或使用已有的 modelId
            String modelId = (String) data.get("modelId");
            if (modelId == null || modelId.isEmpty()) {
                modelId = provider + "-" + (modelName != null ? modelName : "default");
                // 将生成的 modelId 写回节点，以便后续执行时能找到
                data.put("modelId", modelId);
            }

            // 构建 ModelConfig 并持久化到 SQLite
            Double temperature = parseDouble(data.get("temperature"));
            Integer topK = parseInteger(data.get("topK"));
            Double topP = parseDouble(data.get("topP"));

            ModelConfig config = ModelConfig.builder()
                    .id(modelId)
                    .provider(provider)
                    .apiKey(apiKey)
                    .baseUrl(baseUrl)
                    .modelName(modelName)
                    .temperature(temperature)
                    .topK(topK)
                    .topP(topP)
                    .build();

            modelPoolService.upsertModel(config);
            log.info("Auto-persisted model credentials for: {} ({})", modelId, modelName);
        }
    }

    /**
     * 移除工作流中的敏感信息
     * 目前移除所有 LLM 节点中的 apiKey 字段
     */
    private void sanitizeWorkflow(Workflow workflow) {
        if (workflow.getNodes() == null) return;
        for (NodeDefinition node : workflow.getNodes()) {
            if (node.getData() != null) {
                node.getData().remove("apiKey");
            }
        }
    }

    private Double parseDouble(Object obj) {
        if (obj == null) return null;
        try { return Double.parseDouble(obj.toString()); }
        catch (NumberFormatException e) { return null; }
    }

    private Integer parseInteger(Object obj) {
        if (obj == null) return null;
        try { return Integer.parseInt(obj.toString()); }
        catch (NumberFormatException e) { return null; }
    }
}
