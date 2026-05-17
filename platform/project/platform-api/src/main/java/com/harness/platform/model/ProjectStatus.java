package com.harness.platform.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectStatus {
    private String projectId;
    private String ownerId;
    private String name;
    private String status;
    private Map<String, String> phases;
    private Instant createdAt;
    private Instant updatedAt;

    public ProjectStatus() {
        this.projectId = UUID.randomUUID().toString();
        this.name = "";
        this.status = "intake";
        this.phases = new LinkedHashMap<>();
        this.phases.put("intake", "pending");
        this.phases.put("architecture", "pending");
        this.phases.put("backend", "pending");
        this.phases.put("frontend", "pending");
        this.phases.put("flutter", "skipped");
        this.phases.put("blockchain", "skipped");
        this.phases.put("fullstack", "pending");
        this.phases.put("deploy", "pending");
        this.phases.put("delivery", "pending");
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Map<String, String> getPhases() { return phases; }
    public void setPhases(Map<String, String> phases) { this.phases = phases; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
