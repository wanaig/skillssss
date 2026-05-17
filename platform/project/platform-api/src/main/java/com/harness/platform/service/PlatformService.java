package com.harness.platform.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.harness.platform.model.ProjectStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PlatformService {

    private final Path outputsRoot;
    private final ObjectMapper objectMapper;

    public PlatformService(@Value("${harness.platform.root}") String platformRoot) {
        this.outputsRoot = Path.of(platformRoot).toAbsolutePath().normalize();
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        System.out.println("[PlatformAPI] outputsRoot = " + this.outputsRoot);
    }

    public Path getOutputsRoot() {
        return outputsRoot;
    }

    // ── Project creation ──────────────────────────────────────────

    public ProjectStatus createProject(String ownerId, String description) throws IOException {
        String projectId = UUID.randomUUID().toString();
        String name = extractName(description);

        ProjectStatus status = new ProjectStatus();
        status.setProjectId(projectId);
        status.setOwnerId(ownerId);
        status.setName(name);
        status.setStatus("intake");
        status.setCreatedAt(Instant.now());
        status.setUpdatedAt(Instant.now());

        // Create per-project directory
        Path projectDir = outputsRoot.resolve(projectId);
        Files.createDirectories(projectDir);
        Files.createDirectories(projectDir.resolve("pf_intake"));
        Files.createDirectories(projectDir.resolve("pf_delivery"));

        // Write client-input.md
        Path clientInput = projectDir.resolve("client-input.md");
        Files.writeString(clientInput, description);

        // Write project-status.json
        Path statusFile = projectDir.resolve("project-status.json");
        objectMapper.writeValue(statusFile.toFile(), status);

        // Update index
        updateIndex(status);

        return status;
    }

    public void deleteProject(String projectId) throws IOException {
        Path projectDir = outputsRoot.resolve(projectId);
        if (Files.exists(projectDir)) {
            try (var walk = Files.walk(projectDir)) {
                walk.sorted(java.util.Comparator.reverseOrder())
                    .forEach(p -> { try { Files.delete(p); } catch (IOException ignored) {} });
            }
        }
        // Remove from index
        Path indexFile = outputsRoot.resolve("_index.json");
        if (Files.exists(indexFile)) {
            List<ProjectStatus> index = objectMapper.readValue(
                indexFile.toFile(), new TypeReference<List<ProjectStatus>>() {});
            if (index != null) {
                index.removeIf(p -> p.getProjectId().equals(projectId));
                objectMapper.writeValue(indexFile.toFile(), index);
            }
        }
    }

    // ── Queries ───────────────────────────────────────────────────

    /**
     * List all projects by scanning the outputs directory.
     * Reads each project's project-status.json directly — always
     * reflects the latest state written by the orchestrator.
     */
    public List<ProjectStatus> getAllProjects(String ownerId) throws IOException {
        if (!Files.exists(outputsRoot)) {
            return List.of();
        }
        List<ProjectStatus> projects = new ArrayList<>();
        try (var dirs = Files.list(outputsRoot)) {
            for (Path dir : dirs.toList()) {
                if (!Files.isDirectory(dir)) continue;
                Path statusFile = dir.resolve("project-status.json");
                if (Files.exists(statusFile)) {
                    try {
                        ProjectStatus s = objectMapper.readValue(
                            statusFile.toFile(), ProjectStatus.class);
                        if (ownerId == null || ownerId.equals(s.getOwnerId())) {
                            projects.add(s);
                        }
                    } catch (IOException ignored) {
                        // Skip corrupt files
                    }
                }
            }
        }
        projects.sort((a, b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt()));
        return projects;
    }

    public List<ProjectStatus> getPendingProjects(String ownerId) throws IOException {
        return getAllProjects(ownerId).stream()
            .filter(p -> "intake".equals(p.getStatus()))
            .collect(Collectors.toList());
    }

    public ProjectStatus getProjectStatus(String projectId) throws IOException {
        Path statusFile = outputsRoot.resolve(projectId).resolve("project-status.json");
        if (Files.exists(statusFile)) {
            return objectMapper.readValue(statusFile.toFile(), ProjectStatus.class);
        }
        return null;
    }

    public List<String> getProjectLog(String projectId) throws IOException {
        Path logFile = outputsRoot.resolve(projectId).resolve("main-log.md");
        if (Files.exists(logFile)) {
            return Files.readAllLines(logFile);
        }
        return List.of();
    }

    public Map<String, Object> getPrd(String projectId) throws IOException {
        Path prdFile = outputsRoot.resolve(projectId)
            .resolve("pf_intake")
            .resolve("prd.md");
        if (Files.exists(prdFile)) {
            String content = Files.readString(prdFile);
            return Map.of("content", content);
        }
        return Map.of("status", "not_ready");
    }

    public Map<String, Object> getAcceptanceReport(String projectId) throws IOException {
        Path reportFile = outputsRoot.resolve(projectId)
            .resolve("pf_delivery")
            .resolve("acceptance-report.md");
        if (Files.exists(reportFile)) {
            String content = Files.readString(reportFile);
            return Map.of("content", content);
        }
        return Map.of("status", "not_ready");
    }

    // ── Index maintenance ─────────────────────────────────────────

    private void updateIndex(ProjectStatus status) throws IOException {
        Files.createDirectories(outputsRoot);
        Path indexFile = outputsRoot.resolve("_index.json");

        List<ProjectStatus> index;
        if (Files.exists(indexFile)) {
            index = objectMapper.readValue(
                indexFile.toFile(),
                new TypeReference<List<ProjectStatus>>() {}
            );
            if (index == null) index = new ArrayList<>();
        } else {
            index = new ArrayList<>();
        }

        // Replace if exists, otherwise add
        index.removeIf(p -> p.getProjectId().equals(status.getProjectId()));
        index.add(status);

        objectMapper.writeValue(indexFile.toFile(), index);
    }

    /**
     * Update project status both in per-project file and in index.
     * Called by external processes (orchestrator) that write project-status.json directly.
     */
    public void syncProjectFromDisk(String projectId) throws IOException {
        ProjectStatus status = getProjectStatus(projectId);
        if (status != null) {
            updateIndex(status);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────

    private String extractName(String description) {
        if (description == null || description.isBlank()) {
            return "未命名项目";
        }
        // Take first line or first 50 chars
        String firstLine = description.lines()
            .findFirst()
            .orElse(description)
            .trim();
        if (firstLine.length() > 50) {
            firstLine = firstLine.substring(0, 50) + "…";
        }
        return firstLine;
    }
}
