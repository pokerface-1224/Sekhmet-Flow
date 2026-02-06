package com.sekhmet.llmflow.model.graph;

import lombok.Data;
import java.util.Map;

/**
 * 节点定义类
 * 描述工作流图中的节点信息
 */
@Data
public class NodeDefinition {
    /** 节点 ID */
    private String id;
    /** 节点类型 (如 prompt, llm, chat_output) */
    private String type;
    /** 节点在画布上的位置 */
    private Position position;
    /** 节点数据 (配置信息) */
    private Map<String, Object> data;

    /**
     * 位置坐标类
     */
    @Data
    public static class Position {
        /** X 轴坐标 */
        private double x;
        /** Y 轴坐标 */
        private double y;
    }
}
