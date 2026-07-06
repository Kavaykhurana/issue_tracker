package com.issuetracker.dto.response;

import com.issuetracker.entity.enums.MemberRole;
import lombok.*;

@Data
@Builder
public class MemberResponse {
    private Long         id;
    private UserResponse user;
    private MemberRole   role;
}
