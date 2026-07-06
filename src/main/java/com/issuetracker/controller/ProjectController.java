package com.issuetracker.controller;

import com.issuetracker.dto.request.*;
import com.issuetracker.dto.response.*;
import com.issuetracker.entity.User;
import com.issuetracker.service.LabelService;
import com.issuetracker.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final LabelService labelService;

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectService.createProject(request, currentUser));
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> listProjects(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(projectService.listProjectsForUser(currentUser));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProject(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(projectService.getProjectById(id, currentUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProjectRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(projectService.updateProject(id, request, currentUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        projectService.deleteProject(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{projectId}/members")
    public ResponseEntity<MemberResponse> addMember(
            @PathVariable Long projectId,
            @Valid @RequestBody AddMemberRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectService.addMember(projectId, request, currentUser));
    }

    @GetMapping("/{projectId}/members")
    public ResponseEntity<List<MemberResponse>> listMembers(
            @PathVariable Long projectId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(projectService.listMembers(projectId, currentUser));
    }

    @PutMapping("/{projectId}/members/{userId}")
    public ResponseEntity<MemberResponse> updateMemberRole(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @Valid @RequestBody UpdateMemberRoleRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(
                projectService.updateMemberRole(projectId, userId, request, currentUser));
    }

    @DeleteMapping("/{projectId}/members/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @AuthenticationPrincipal User currentUser) {
        projectService.removeMember(projectId, userId, currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{projectId}/stats")
    public ResponseEntity<ProjectStatsResponse> getStats(
            @PathVariable Long projectId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(projectService.getStats(projectId, currentUser));
    }

    @PostMapping("/{projectId}/labels")
    public ResponseEntity<LabelResponse> createLabel(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateLabelRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(labelService.createLabel(projectId, request, currentUser));
    }

    @GetMapping("/{projectId}/labels")
    public ResponseEntity<List<LabelResponse>> listLabels(
            @PathVariable Long projectId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(labelService.listLabels(projectId, currentUser));
    }
}
