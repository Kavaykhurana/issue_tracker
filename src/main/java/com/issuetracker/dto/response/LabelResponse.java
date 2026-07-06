package com.issuetracker.dto.response;

import lombok.*;

@Data
@Builder
public class LabelResponse {
    private Long   id;
    private String name;
    private String color;
}
