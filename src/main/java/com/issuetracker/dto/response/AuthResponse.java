package com.issuetracker.dto.response;

import lombok.*;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String tokenType;
    private String token;
    private UserResponse user;
}
