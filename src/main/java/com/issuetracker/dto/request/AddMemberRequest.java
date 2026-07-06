package com.issuetracker.dto.request;

import com.issuetracker.entity.enums.MemberRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddMemberRequest {

    @NotNull
    private Long userId;

    @NotNull
    private MemberRole role;
}
