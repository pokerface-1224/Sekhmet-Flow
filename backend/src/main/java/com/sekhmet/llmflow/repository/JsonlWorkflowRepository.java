package com.sekhmet.llmflow.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sekhmet.llmflow.model.entity.Workflow;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class JsonlWorkflowRepository {

    @Value("${sekhmet.data-dir}")
    private String dataDir;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Path workflowFile;

    @PostConstruct
    public void init() throws IOException {
        Path dir = Paths.get(dataDir);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        workflowFile = dir.resolve("workflows.jsonl");
        if (!Files.exists(workflowFile)) {
            Files.createFile(workflowFile);
        }
    }

    public List<Workflow> findAll() {
        try {
            return Files.lines(workflowFile)
                    .map(line -> {
                        try {
                            return objectMapper.readValue(line, Workflow.class);
                        } catch (IOException e) {
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public void save(Workflow workflow) {
        List<Workflow> all = findAll();
        // Remove existing with same ID to update
        all.removeIf(w -> w.getId().equals(workflow.getId()));
        all.add(workflow);

        try (BufferedWriter writer = Files.newBufferedWriter(workflowFile)) {
            for (Workflow w : all) {
                writer.write(objectMapper.writeValueAsString(w));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save workflow", e);
        }
    }
}
