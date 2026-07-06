package com.issuetracker.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProjectRequest {

    @Size(max = 150)
    private String name;

    private String description;
}
