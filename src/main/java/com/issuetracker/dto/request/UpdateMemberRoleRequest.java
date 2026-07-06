package com.issuetracker.dto.request;

import com.issuetracker.entity.enums.MemberRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateMemberRoleRequest {

    @NotNull
    private MemberRole role;
}
