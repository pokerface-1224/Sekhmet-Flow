package com.sekhmet.llmflow.model.entity;

import com.sekhmet.llmflow.model.graph.EdgeDefinition;
import com.sekhmet.llmflow.model.graph.NodeDefinition;
import lombok.Data;
import java.util.List;

@Data
public class Workflow {
    private String id;
    private String name;
    private List<NodeDefinition> nodes;
    private List<EdgeDefinition> edges;
}
