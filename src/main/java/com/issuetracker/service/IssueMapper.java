package com.issuetracker.service;

import com.issuetracker.dto.response.*;
import com.issuetracker.entity.*;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public final class IssueMapper {

    private IssueMapper() {}

    public static UserResponse toUserResponse(User user) {
        if (user == null) return null;
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .build();
    }

    public static LabelResponse toLabelResponse(IssueLabel label) {
        return LabelResponse.builder()
                .id(label.getId())
                .name(label.getName())
                .color(label.getColor())
                .build();
    }

    public static CommentResponse toCommentResponse(IssueComment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .author(toUserResponse(comment.getAuthor()))
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    public static IssueResponse toSummaryResponse(Issue issue) {
        return IssueResponse.builder()
                .id(issue.getId())
                .projectId(issue.getProject().getId())
                .title(issue.getTitle())
                .description(issue.getDescription())
                .status(issue.getStatus())
                .priority(issue.getPriority())
                .type(issue.getType())
                .assignee(toUserResponse(issue.getAssignee()))
                .reporter(toUserResponse(issue.getReporter()))
                .parentIssueId(issue.getParentIssue() != null
                        ? issue.getParentIssue().getId() : null)
                .dueDate(issue.getDueDate())
                .createdAt(issue.getCreatedAt())
                .updatedAt(issue.getUpdatedAt())
                .labels(issue.getLabels().stream()
                        .map(IssueMapper::toLabelResponse)
                        .collect(Collectors.toSet()))
                .build();
    }

    public static IssueResponse toDetailResponse(Issue issue) {
        IssueResponse response = toSummaryResponse(issue);

        response.setComments(issue.getComments().stream()
                .map(IssueMapper::toCommentResponse)
                .collect(Collectors.toList()));

        response.setSubTasks(issue.getSubTasks().stream()
                .map(IssueMapper::toSummaryResponse)
                .collect(Collectors.toList()));

        return response;
    }
}
