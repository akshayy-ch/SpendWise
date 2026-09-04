package com.spendwise.dto.request.group;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class CreateGroupRequest {

    @NotBlank(message = "Name is a required field for group creation")
    @Size(min = 3, max = 50)
    private String name;

    @Size(min = 3, max = 255)
    private String description;

}
