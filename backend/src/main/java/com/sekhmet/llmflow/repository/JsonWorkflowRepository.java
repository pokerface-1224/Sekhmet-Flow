package com.sekhmet.llmflow.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sekhmet.llmflow.model.entity.Workflow;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 基于独立 JSON 文件的工作流存储库
 * 每个工作流保存为 data/workflows/{id}.json
 * 支持从旧的 workflows.jsonl 自动迁移
 */
@Repository
@Slf4j
public class JsonWorkflowRepository {

    @Value("${sekhmet.data-dir}")
    private String dataDir;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);
    private Path workflowsDir;

    /**
     * 初始化方法
     * 确保 workflows 目录存在，并执行旧数据迁移
     */
    @PostConstruct
    public void init() throws IOException {
        workflowsDir = Paths.get(dataDir, "workflows");
        if (!Files.exists(workflowsDir)) {
            Files.createDirectories(workflowsDir);
        }
        // 自动迁移旧的 JSONL 数据
        migrateFromJsonl();
    }

    /**
     * 查找所有工作流
     * @return 工作流列表
     */
    public List<Workflow> findAll() {
        try (Stream<Path> paths = Files.list(workflowsDir)) {
            return paths
                    .filter(p -> p.toString().endsWith(".json"))
                    .map(this::readWorkflowFile)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Failed to list workflow files", e);
            return Collections.emptyList();
        }
    }

    /**
     * 根据 ID 查找工作流
     * @param id 工作流 ID
     * @return 工作流对象，不存在则返回 null
     */
    public Workflow findById(String id) {
        Path file = workflowsDir.resolve(id + ".json");
        if (!Files.exists(file)) {
            return null;
        }
        return readWorkflowFile(file);
    }

    /**
     * 保存工作流 (写入独立 JSON 文件)
     * @param workflow 要保存的工作流对象
     */
    public void save(Workflow workflow) {
        Path file = workflowsDir.resolve(workflow.getId() + ".json");
        try {
            objectMapper.writeValue(file.toFile(), workflow);
            log.info("Workflow saved: {}", file.getFileName());
        } catch (IOException e) {
            throw new RuntimeException("Failed to save workflow: " + workflow.getId(), e);
        }
    }

    /**
     * 删除工作流
     * @param id 工作流 ID
     */
    public void deleteById(String id) {
        Path file = workflowsDir.resolve(id + ".json");
        try {
            if (Files.deleteIfExists(file)) {
                log.info("Workflow deleted: {}", file.getFileName());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete workflow: " + id, e);
        }
    }

    /**
     * 读取单个工作流文件
     */
    private Workflow readWorkflowFile(Path file) {
        try {
            return objectMapper.readValue(file.toFile(), Workflow.class);
        } catch (IOException e) {
            log.error("Failed to read workflow file: {}", file.getFileName(), e);
            return null;
        }
    }

    /**
     * 从旧的 workflows.jsonl 迁移数据
     * 将每条记录拆分为独立 JSON 文件，迁移后重命名旧文件为 .bak
     * 迁移过程中同时移除 apiKey 字段
     */
    private void migrateFromJsonl() {
        Path oldFile = Paths.get(dataDir, "workflows.jsonl");
        if (!Files.exists(oldFile)) {
            return;
        }

        log.info("Detected old workflows.jsonl, starting migration...");
        int migrated = 0;

        try {
            // 使用非格式化的 ObjectMapper 读取旧数据
            ObjectMapper plainMapper = new ObjectMapper();
            List<String> lines = Files.readAllLines(oldFile);

            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) continue;

                try {
                    Workflow workflow = plainMapper.readValue(trimmed, Workflow.class);
                    if (workflow.getId() == null || workflow.getId().isEmpty()) {
                        workflow.setId(UUID.randomUUID().toString());
                    }

                    // 迁移时同时清除 apiKey
                    if (workflow.getNodes() != null) {
                        workflow.getNodes().forEach(node -> {
                            if (node.getData() != null) {
                                node.getData().remove("apiKey");
                            }
                        });
                    }

                    // 保存为独立文件
                    save(workflow);
                    migrated++;
                } catch (IOException e) {
                    log.warn("Failed to parse workflow line, skipping: {}", e.getMessage());
                }
            }

            // 重命名旧文件为 .bak
            Path backupFile = Paths.get(dataDir, "workflows.jsonl.bak");
            Files.move(oldFile, backupFile, StandardCopyOption.REPLACE_EXISTING);
            log.info("Migration complete: {} workflows migrated. Old file renamed to workflows.jsonl.bak", migrated);

        } catch (IOException e) {
            log.error("Migration failed", e);
        }
    }
}
