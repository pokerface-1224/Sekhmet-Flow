package com.sekhmet.llmflow.controller;

import com.sekhmet.llmflow.model.entity.Workflow;
import com.sekhmet.llmflow.service.WorkflowService;
import com.sekhmet.llmflow.service.engine.WorkflowEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


/**
 * 工作流执行控制器
 * 处理工作流的运行请求
 */
@RestController
@RequestMapping("/api/workflows")
@RequiredArgsConstructor
public class ExecutionController {

    private final WorkflowService workflowService;
    private final WorkflowEngine workflowEngine;

    /**
     * 执行指定ID的工作流
     * @param id 工作流ID
     * @param inputs 输入参数 (Key-Value 映射)
     * @return 执行结果
     */
    @PostMapping("/{id}/run")
    public com.sekhmet.llmflow.model.dto.ExecutionResult runWorkflow(@PathVariable String id,
            @RequestBody(required = false) Map<String, Object> inputs) {
        Workflow workflow = workflowService.getWorkflow(id);
        if (workflow == null) {
            throw new RuntimeException("Workflow not found");
        }
        return workflowEngine.execute(workflow, inputs != null ? inputs : Map.of());
    }
}
