package com.issuetracker.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
public class ProjectResponse {
    private Long          id;
    private String        name;
    private String        key;
    private String        description;
    private UserResponse  owner;
    private LocalDateTime createdAt;
    private int           memberCount;
    private long          totalIssues;
    private long          openIssues;
}
