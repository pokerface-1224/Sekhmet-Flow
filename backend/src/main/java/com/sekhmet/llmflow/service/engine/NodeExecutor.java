package com.sekhmet.llmflow.service.engine;

import com.sekhmet.llmflow.model.graph.NodeDefinition;
import java.util.Map;

public interface NodeExecutor {
    // Execute the node logic provided inputs from previous nodes
    // Returns the outputs of this node
    Map<String, Object> execute(NodeDefinition node, Map<String, Object> inputs);

    // The type of node this executor handles (e.g. "llm-node")
    String getNodeType();
}
