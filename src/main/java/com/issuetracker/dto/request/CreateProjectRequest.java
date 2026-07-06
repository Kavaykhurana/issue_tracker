package com.issuetracker.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateProjectRequest {

    @NotBlank
    @Size(max = 150)
    private String name;

    @NotBlank
    @Size(min = 2, max = 10)
    @Pattern(regexp = "^[A-Z]{2,10}$", message = "Key must be 2-10 uppercase letters")
    private String key;

    private String description;
}
