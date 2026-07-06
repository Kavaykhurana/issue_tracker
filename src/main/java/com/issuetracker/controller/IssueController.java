package com.issuetracker.controller;

import com.issuetracker.dto.request.*;
import com.issuetracker.dto.response.IssueResponse;
import com.issuetracker.entity.User;
import com.issuetracker.entity.enums.*;
import com.issuetracker.service.IssueService;
import com.issuetracker.service.LabelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class IssueController {

    private final IssueService issueService;
    private final LabelService labelService;

    @GetMapping("/api/v1/projects/{projectId}/issues")
    public ResponseEntity<Page<IssueResponse>> listIssues(
            @PathVariable Long projectId,
            @RequestParam(required = false) IssueStatus   status,
            @RequestParam(required = false) IssuePriority priority,
            @RequestParam(required = false) IssueType     type,
            @RequestParam(required = false) Long          assigneeId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal User currentUser) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        return ResponseEntity.ok(
                issueService.listIssues(projectId, status, priority,
                        type, assigneeId, pageable, currentUser));
    }

    @PostMapping("/api/v1/projects/{projectId}/issues")
    public ResponseEntity<IssueResponse> createIssue(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateIssueRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(issueService.createIssue(projectId, request, currentUser));
    }

    @GetMapping("/api/v1/issues/{id}")
    public ResponseEntity<IssueResponse> getIssue(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(issueService.getIssueById(id, currentUser));
    }

    @PutMapping("/api/v1/issues/{id}")
    public ResponseEntity<IssueResponse> updateIssue(
            @PathVariable Long id,
            @Valid @RequestBody UpdateIssueRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(issueService.updateIssue(id, request, currentUser));
    }

    @DeleteMapping("/api/v1/issues/{id}")
    public ResponseEntity<Void> deleteIssue(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        issueService.deleteIssue(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/api/v1/issues/{id}/status")
    public ResponseEntity<IssueResponse> transitionStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusTransitionRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(
                issueService.transitionStatus(id, request, currentUser));
    }

    @GetMapping("/api/v1/issues/assigned-to-me")
    public ResponseEntity<List<IssueResponse>> assignedToMe(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(issueService.getIssuesAssignedToMe(currentUser));
    }

    @PostMapping("/api/v1/issues/{issueId}/labels/{labelId}")
    public ResponseEntity<IssueResponse> addLabel(
            @PathVariable Long issueId,
            @PathVariable Long labelId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(
                labelService.addLabelToIssue(issueId, labelId, currentUser));
    }

    @DeleteMapping("/api/v1/issues/{issueId}/labels/{labelId}")
    public ResponseEntity<IssueResponse> removeLabel(
            @PathVariable Long issueId,
            @PathVariable Long labelId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(
                labelService.removeLabelFromIssue(issueId, labelId, currentUser));
    }
}
