package com.spendwise.dto.response.user;

import com.spendwise.enums.Role;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
public class AdminUserResponse {

    private UUID id;
    private String username;
    private String name;
    private String email;
    private String phoneNumber;
    private Role role;
}