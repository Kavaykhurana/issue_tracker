package com.issuetracker.dto.response;

import com.issuetracker.entity.enums.IssuePriority;
import com.issuetracker.entity.enums.IssueStatus;
import com.issuetracker.entity.enums.IssueType;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueResponse {
    private Long          id;
    private Long          projectId;
    private String        title;
    private String        description;
    private IssueStatus   status;
    private IssuePriority priority;
    private IssueType     type;
    private UserResponse  assignee;
    private UserResponse  reporter;
    private Long          parentIssueId;
    private LocalDate     dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<CommentResponse> comments;
    private List<IssueResponse>   subTasks;
    private Set<LabelResponse>    labels;
}
