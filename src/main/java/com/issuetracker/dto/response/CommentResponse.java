package com.issuetracker.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentResponse {
    private Long          id;
    private UserResponse  author;
    private String        content;
    private LocalDateTime createdAt;
}
