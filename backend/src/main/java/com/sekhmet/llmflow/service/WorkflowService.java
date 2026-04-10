package com.sekhmet.llmflow.service;

import com.sekhmet.llmflow.model.entity.Workflow;
import com.sekhmet.llmflow.model.graph.NodeDefinition;
import com.sekhmet.llmflow.repository.JsonWorkflowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * 工作流业务逻辑服务类
 * 处理工作流的存储和检索
 */
@Service
@RequiredArgsConstructor
public class WorkflowService {
    private final JsonWorkflowRepository workflowRepository;

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
     * 保存前会移除节点中的敏感信息 (apiKey)
     * @param workflow 工作流对象
     * @return 保存后的工作流
     */
    public Workflow saveWorkflow(Workflow workflow) {
        if (workflow.getId() == null || workflow.getId().isEmpty()) {
            workflow.setId(UUID.randomUUID().toString());
        }
        // 保存前移除敏感信息
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
}
