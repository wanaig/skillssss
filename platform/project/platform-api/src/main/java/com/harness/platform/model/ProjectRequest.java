package com.harness.platform.model;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public class ProjectRequest {
    @NotBlank(message = "项目需求描述不能为空")
    private String description;
    private Map<String, String> constraints;

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Map<String, String> getConstraints() { return constraints; }
    public void setConstraints(Map<String, String> constraints) { this.constraints = constraints; }
}
