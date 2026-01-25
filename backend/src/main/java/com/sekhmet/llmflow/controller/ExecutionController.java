package com.sekhmet.llmflow.controller;

import com.sekhmet.llmflow.model.entity.Workflow;
import com.sekhmet.llmflow.service.WorkflowService;
import com.sekhmet.llmflow.service.engine.WorkflowEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/workflows")
@RequiredArgsConstructor
public class ExecutionController {

    private final WorkflowService workflowService;
    private final WorkflowEngine workflowEngine;

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
