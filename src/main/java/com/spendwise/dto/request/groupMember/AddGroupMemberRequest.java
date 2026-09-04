package com.spendwise.dto.request.groupMember;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddGroupMemberRequest {

    @NotEmpty(message = "At least one user is required")
    private List<UUID> userIds;
}