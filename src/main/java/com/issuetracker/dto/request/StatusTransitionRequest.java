package com.issuetracker.dto.request;

import com.issuetracker.entity.enums.IssueStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatusTransitionRequest {

    @NotNull
    private IssueStatus newStatus;
}
