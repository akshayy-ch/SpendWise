package com.spendwise.dto.request.user;

import com.spendwise.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class UpdateUserRoleRequest {

    @NotNull
    private Role role;

}
