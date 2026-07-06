package com.issuetracker.dto.response;

import lombok.*;

@Data
@Builder
public class UserResponse {
    private Long   id;
    private String username;
    private String email;
    private String displayName;
}
