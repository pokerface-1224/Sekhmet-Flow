package com.sekhmet.llmflow.service;

import com.sekhmet.llmflow.model.entity.Workflow;
import com.sekhmet.llmflow.repository.JsonlWorkflowRepository;
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
    private final JsonlWorkflowRepository workflowRepository;

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
     * @param workflow 工作流对象
     * @return 保存后的工作流
     */
    public Workflow saveWorkflow(Workflow workflow) {
        if (workflow.getId() == null || workflow.getId().isEmpty()) {
            workflow.setId(UUID.randomUUID().toString());
        }
        workflowRepository.save(workflow);
        return workflow;
    }

    /**
     * 根据 ID 获取工作流
     * @param id 工作流 ID
     * @return 工作流对象，如果不存在则返回 null
     */
    public Workflow getWorkflow(String id) {
        return workflowRepository.findAll().stream()
                .filter(w -> w.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}
