package com.issuetracker.controller;

import com.issuetracker.dto.request.CreateCommentRequest;
import com.issuetracker.dto.response.CommentResponse;
import com.issuetracker.entity.User;
import com.issuetracker.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/api/v1/issues/{issueId}/comments")
    public ResponseEntity<List<CommentResponse>> listComments(
            @PathVariable Long issueId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(commentService.listComments(issueId, currentUser));
    }

    @PostMapping("/api/v1/issues/{issueId}/comments")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long issueId,
            @Valid @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.addComment(issueId, request, currentUser));
    }

    @PutMapping("/api/v1/comments/{id}")
    public ResponseEntity<CommentResponse> editComment(
            @PathVariable Long id,
            @Valid @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(commentService.editComment(id, request, currentUser));
    }

    @DeleteMapping("/api/v1/comments/{id}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        commentService.deleteComment(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
