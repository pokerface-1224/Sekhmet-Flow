package com.sekhmet.llmflow.controller;

import com.sekhmet.llmflow.model.entity.Workflow;
import com.sekhmet.llmflow.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workflows")
@RequiredArgsConstructor
public class WorkflowController {
    private final WorkflowService workflowService;

    @GetMapping
    public List<Workflow> getAllWorkflows() {
        return workflowService.getAllWorkflows();
    }

    @PostMapping
    public Workflow saveWorkflow(@RequestBody Workflow workflow) {
        return workflowService.saveWorkflow(workflow);
    }

    @GetMapping("/{id}")
    public Workflow getWorkflow(@PathVariable String id) {
        return workflowService.getWorkflow(id);
    }
}
