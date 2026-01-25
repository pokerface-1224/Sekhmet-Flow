package com.sekhmet.llmflow.service;

import com.sekhmet.llmflow.model.entity.Workflow;
import com.sekhmet.llmflow.repository.JsonlWorkflowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkflowService {
    private final JsonlWorkflowRepository workflowRepository;

    public List<Workflow> getAllWorkflows() {
        return workflowRepository.findAll();
    }

    public Workflow saveWorkflow(Workflow workflow) {
        if (workflow.getId() == null || workflow.getId().isEmpty()) {
            workflow.setId(UUID.randomUUID().toString());
        }
        workflowRepository.save(workflow);
        return workflow;
    }

    public Workflow getWorkflow(String id) {
        return workflowRepository.findAll().stream()
                .filter(w -> w.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}
