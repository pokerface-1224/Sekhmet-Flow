package com.sekhmet.llmflow.model.graph;

import lombok.Data;

@Data
public class EdgeDefinition {
    private String id;
    private String source;
    private String target;
    private String sourceHandle;
    private String targetHandle;
}
