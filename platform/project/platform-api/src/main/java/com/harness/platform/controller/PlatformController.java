package com.harness.platform.controller;

import com.harness.platform.model.ProjectRequest;
import com.harness.platform.model.ProjectStatus;
import com.harness.platform.service.PlatformService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class PlatformController {

    private final PlatformService platformService;

    public PlatformController(PlatformService platformService) {
        this.platformService = platformService;
    }

    private String getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof String userId) {
            return userId;
        }
        return null;
    }

    @PostMapping("/projects")
    public ResponseEntity<ProjectStatus> createProject(@Valid @RequestBody ProjectRequest request) {
        try {
            String userId = getCurrentUserId();
            ProjectStatus status = platformService.createProject(userId, request.getDescription());
            return ResponseEntity.ok(status);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/projects")
    public ResponseEntity<List<ProjectStatus>> getProjects() {
        try {
            return ResponseEntity.ok(platformService.getAllProjects(getCurrentUserId()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/debug/path")
    public ResponseEntity<Map<String, String>> debugPath() {
        return ResponseEntity.ok(Map.of(
            "outputsRoot", platformService.getOutputsRoot().toString(),
            "exists", String.valueOf(java.nio.file.Files.exists(platformService.getOutputsRoot()))
        ));
    }

    @GetMapping("/projects/pending")
    public ResponseEntity<List<ProjectStatus>> getPendingProjects() {
        try {
            return ResponseEntity.ok(platformService.getPendingProjects(getCurrentUserId()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/projects/{projectId}/status")
    public ResponseEntity<ProjectStatus> getProjectStatus(@PathVariable String projectId) {
        try {
            ProjectStatus status = platformService.getProjectStatus(projectId);
            if (status == null) {
                return ResponseEntity.notFound().build();
            }
            String userId = getCurrentUserId();
            if (userId != null && status.getOwnerId() != null && !userId.equals(status.getOwnerId())) {
                return ResponseEntity.status(403).build();
            }
            return ResponseEntity.ok(status);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/projects/{projectId}/log")
    public ResponseEntity<Object> getProjectLog(@PathVariable String projectId) {
        try {
            List<String> lines = platformService.getProjectLog(projectId);
            return ResponseEntity.ok(Map.of("lines", lines));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/projects/{projectId}/prd")
    public ResponseEntity<Object> getPrd(@PathVariable String projectId) {
        try {
            Object prd = platformService.getPrd(projectId);
            return ResponseEntity.ok(prd);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/projects/{projectId}/report")
    public ResponseEntity<Object> getAcceptanceReport(@PathVariable String projectId) {
        try {
            Object report = platformService.getAcceptanceReport(projectId);
            return ResponseEntity.ok(report);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/projects/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable String projectId) {
        try {
            platformService.deleteProject(projectId);
            return ResponseEntity.noContent().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/projects/{projectId}/sync")
    public ResponseEntity<ProjectStatus> syncProject(@PathVariable String projectId) {
        try {
            platformService.syncProjectFromDisk(projectId);
            ProjectStatus status = platformService.getProjectStatus(projectId);
            return ResponseEntity.ok(status);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
