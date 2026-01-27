package com.sekhmet.llmflow.service.engine;

import com.sekhmet.llmflow.model.entity.Workflow;
import com.sekhmet.llmflow.model.graph.EdgeDefinition;
import com.sekhmet.llmflow.model.graph.NodeDefinition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;


/**
 * 工作流引擎
 * 负责解析工作流图并调度执行节点
 */
@Service
@RequiredArgsConstructor
public class WorkflowEngine {

    private final List<NodeExecutor> nodeExecutors;
    private Map<String, NodeExecutor> executorMap;

    @jakarta.annotation.PostConstruct
    public void init() {
        executorMap = new HashMap<>();
        for (NodeExecutor executor : nodeExecutors) {
            executorMap.put(executor.getNodeType(), executor);
        }
    }

    /**
     * 执行工作流
     * @param workflow 工作流对象
     * @param initialInputs 初始输入
     * @return 执行结果
     */
    public com.sekhmet.llmflow.model.dto.ExecutionResult execute(Workflow workflow, Map<String, Object> initialInputs) {
        long workflowStartTime = System.currentTimeMillis();

        // 1. 构建邻接表
        Map<String, List<String>> adj = new HashMap<>();
        Map<String, Integer> inDegree = new HashMap<>();
        Map<String, NodeDefinition> nodeMap = new HashMap<>();

        for (NodeDefinition node : workflow.getNodes()) {
            adj.putIfAbsent(node.getId(), new ArrayList<>());
            inDegree.put(node.getId(), 0);
            nodeMap.put(node.getId(), node);
        }

        for (EdgeDefinition edge : workflow.getEdges()) {
            adj.get(edge.getSource()).add(edge.getTarget());
            inDegree.merge(edge.getTarget(), 1, (a, b) -> a + b);
        }

        // 2. 拓扑排序 (Kahn 算法)
        Queue<String> queue = new LinkedList<>();
        for (String nodeId : inDegree.keySet()) {
            if (inDegree.get(nodeId) == 0) {
                queue.add(nodeId);
            }
        }

        // Store outputs to downstream nodes (raw map)
        Map<String, Map<String, Object>> nodeOutputs = new HashMap<>();
        // Store detailed results
        Map<String, com.sekhmet.llmflow.model.dto.NodeResult> nodeResults = new HashMap<>();

        while (!queue.isEmpty()) {
            String nodeId = queue.poll();
            NodeDefinition node = nodeMap.get(nodeId);
            long nodeStartTime = System.currentTimeMillis();

            // Gather inputs from parents
            Map<String, Object> inputs = new HashMap<>();
            inputs.putAll(initialInputs);

            // Find incoming edges to get outputs from parents
            for (EdgeDefinition edge : workflow.getEdges()) {
                if (edge.getTarget().equals(nodeId)) {
                    String parentId = edge.getSource();
                    if (nodeOutputs.containsKey(parentId)) {
                        inputs.putAll(nodeOutputs.get(parentId));
                    }
                }
            }

            // Execute
            NodeExecutor executor = executorMap.get(node.getType());
            Map<String, Object> output = new HashMap<>();
            String error = null;
            String status = "SUCCESS";

            if (executor != null) {
                try {
                    output = executor.execute(node, inputs);
                } catch (Exception e) {
                    System.err.println("Error executing node " + nodeId + ": " + e.getMessage());
                    output.put("error", e.getMessage());
                    error = e.getMessage();
                    status = "ERROR";
                }
            } else {
                System.out.println("No executor for type: " + node.getType());
                status = "SKIPPED";
                error = "No executor found";
            }

            long duration = System.currentTimeMillis() - nodeStartTime;

            nodeOutputs.put(nodeId, output);

            // Build NodeResult
            nodeResults.put(nodeId, com.sekhmet.llmflow.model.dto.NodeResult.builder()
                    .nodeId(nodeId)
                    .status(status)
                    .inputs(new HashMap<>(inputs)) // Copy inputs
                    .outputs(output)
                    .error(error)
                    .durationMs(duration)
                    .build());

            // Update neighbors
            if (adj.containsKey(nodeId)) {
                for (String neighbor : adj.get(nodeId)) {
                    inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                    if (inDegree.get(neighbor) == 0) {
                        queue.add(neighbor);
                    }
                }
            }
        }

        long workflowDuration = System.currentTimeMillis() - workflowStartTime;

        boolean hasError = nodeResults.values().stream()
                .anyMatch(result -> "ERROR".equals(result.getStatus()));

        return com.sekhmet.llmflow.model.dto.ExecutionResult.builder()
                .workflowId(workflow.getId())
                .status(hasError ? "FAILED" : "COMPLETED")
                .nodeResults(nodeResults)
                .totalDurationMs(workflowDuration)
                .build();
    }
}
