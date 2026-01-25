package com.sekhmet.llmflow.model.graph;

import lombok.Data;
import java.util.Map;

@Data
public class NodeDefinition {
    private String id;
    private String type;
    private Position position;
    private Map<String, Object> data;

    @Data
    public static class Position {
        private double x;
        private double y;
    }
}
