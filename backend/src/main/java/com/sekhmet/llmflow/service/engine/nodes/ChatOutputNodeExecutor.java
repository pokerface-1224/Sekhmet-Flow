package com.sekhmet.llmflow.service.engine.nodes;

import com.sekhmet.llmflow.model.graph.NodeDefinition;
import com.sekhmet.llmflow.service.engine.NodeExecutor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ChatOutputNodeExecutor implements NodeExecutor {

    @Override
    public Map<String, Object> execute(NodeDefinition node, Map<String, Object> inputs) {
        Map<String, Object> output = new HashMap<>();
        String text = "";

        // Aggregate text inputs
        if (inputs.containsKey("text")) {
            text = (String) inputs.get("text");
        } else if (inputs.containsKey("response")) {
            text = (String) inputs.get("response"); // Support response key from LlmNode
        } else {
            // Concatenate all strings
            for (Object val : inputs.values()) {
                if (val instanceof String) {
                    text += (String) val + "\n";
                }
            }
        }

        output.put("output", text.trim());
        return output;
    }

    @Override
    public String getNodeType() {
        return "chat-output-node";
    }
}
