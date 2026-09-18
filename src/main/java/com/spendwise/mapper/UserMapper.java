package com.spendwise.mapper;

import com.spendwise.dto.response.user.AdminUserResponse;
import com.spendwise.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    AdminUserResponse toResponse(User user);
}
