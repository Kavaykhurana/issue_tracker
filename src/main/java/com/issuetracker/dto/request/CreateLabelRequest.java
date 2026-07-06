package com.issuetracker.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateLabelRequest {

    @NotBlank
    @Size(max = 50)
    private String name;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be a hex code like #FF0000")
    private String color;
}
