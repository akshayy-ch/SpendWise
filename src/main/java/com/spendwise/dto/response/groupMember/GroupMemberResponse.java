package com.spendwise.dto.response.groupMember;

import com.spendwise.enums.GroupMemberStatus;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupMemberResponse {

    private UUID userId;
    private String userName;
    private GroupMemberStatus status;
    private OffsetDateTime joinedAt;
}