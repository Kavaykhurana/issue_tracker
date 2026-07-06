package com.issuetracker.dto.request;

import com.issuetracker.entity.enums.IssuePriority;
import com.issuetracker.entity.enums.IssueStatus;
import com.issuetracker.entity.enums.IssueType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateIssueRequest {

    @NotBlank
    @Size(max = 200)
    private String title;

    private String description;

    private IssueStatus status;

    private IssuePriority priority;

    private IssueType type;

    private Long assigneeId;

    private Long parentIssueId;

    private LocalDate dueDate;
}
