package com.issuetracker.dto.request;

import com.issuetracker.entity.enums.IssuePriority;
import com.issuetracker.entity.enums.IssueType;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateIssueRequest {

    @Size(max = 200)
    private String title;

    private String description;

    private IssuePriority priority;

    private IssueType type;

    private Long assigneeId;

    private LocalDate dueDate;
}
