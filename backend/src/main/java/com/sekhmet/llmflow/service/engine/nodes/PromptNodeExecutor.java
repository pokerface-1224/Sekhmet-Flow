package com.sekhmet.llmflow.service.engine.nodes;

import com.sekhmet.llmflow.model.graph.NodeDefinition;
import com.sekhmet.llmflow.service.engine.NodeExecutor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PromptNodeExecutor implements NodeExecutor {

    @Override
    public Map<String, Object> execute(NodeDefinition node, Map<String, Object> inputs) {
        Map<String, Object> output = new HashMap<>();
        // In Vue Flow PromptNode, user enters text in 'label' or specific data field
        // Assuming data.label or data.text
        if (node.getData() != null) {
            if (node.getData().containsKey("text")) {
                output.put("text", node.getData().get("text"));
            } else if (node.getData().containsKey("label")) {
                output.put("text", node.getData().get("label"));
            }
        }
        return output;
    }

    @Override
    public String getNodeType() {
        return "prompt-node";
    }
}
