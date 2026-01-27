package com.sekhmet.llmflow.controller;

import com.sekhmet.llmflow.model.entity.Workflow;
import com.sekhmet.llmflow.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 工作流管理控制器
 * 提供工作流的增删改查接口
 */
@RestController
@RequestMapping("/api/workflows")
@RequiredArgsConstructor
public class WorkflowController {
    private final WorkflowService workflowService;

    /**
     * 获取所有工作流列表
     * @return 工作流列表
     */
    @GetMapping
    public List<Workflow> getAllWorkflows() {
        return workflowService.getAllWorkflows();
    }

    /**
     * 保存或更新工作流
     * @param workflow 工作流对象
     * @return 保存后的工作流
     */
    @PostMapping
    public Workflow saveWorkflow(@RequestBody Workflow workflow) {
        return workflowService.saveWorkflow(workflow);
    }

    /**
     * 根据ID获取工作流详情
     * @param id 工作流ID
     * @return 工作流详情
     */
    @GetMapping("/{id}")
    public Workflow getWorkflow(@PathVariable String id) {
        return workflowService.getWorkflow(id);
    }
}
