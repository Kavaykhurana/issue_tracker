package com.issuetracker.dto.response;

import lombok.*;

import java.util.Map;

@Data
@Builder
public class ProjectStatsResponse {
    private Long              projectId;
    private long              totalIssues;
    private Map<String, Long> byStatus;
    private Map<String, Long> byPriority;
    private Map<String, Long> byAssignee;
}
