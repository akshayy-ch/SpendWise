package com.spendwise.mapper;

import com.spendwise.dto.request.group.CreateGroupRequest;
import com.spendwise.dto.response.group.GroupResponse;
import com.spendwise.entity.Group;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GroupMapper {

    Group toEntity(CreateGroupRequest request);

    @Mapping(target = "creatorName", source = "user.name")
    @Mapping(target = "status", source = "status")
    GroupResponse toResponse(Group group);
}