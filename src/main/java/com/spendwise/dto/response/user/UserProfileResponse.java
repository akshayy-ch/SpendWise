package com.spendwise.dto.response.user;

import com.spendwise.enums.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponse {
    private String username;
    private String name;
    private String email;
    private String phoneNumber;
    private Role role;
}