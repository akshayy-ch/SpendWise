package com.spendwise.entity;

import com.spendwise.enums.GroupMemberStatus;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "group_members")

public class GroupMember implements Serializable {

    @EmbeddedId
    private GroupMemberId id = new GroupMemberId();

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private GroupMemberStatus status;

    @Column(name = "joined_at", nullable = false, updatable = false, insertable = false)
    private OffsetDateTime joinedAt;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false)
    private OffsetDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("groupId")
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
