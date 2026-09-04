package com.spendwise.repository;

import com.spendwise.entity.GroupMember;
import com.spendwise.entity.GroupMemberId;
import com.spendwise.enums.GroupMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupMemberRepository extends JpaRepository<GroupMember, GroupMemberId> {

    Optional<GroupMember> findByGroupIdAndUserId(UUID groupId, UUID userId);

    List<GroupMember> findAllByGroupId(UUID groupId);

    List<GroupMember> findAllByGroupIdAndStatus(UUID groupId, GroupMemberStatus status);

    Optional<GroupMember> findByIdGroupIdAndIdUserId(UUID groupId, UUID userId);
}
