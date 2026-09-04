package com.spendwise.dto.request.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class CreateCategoryRequest {

    @NotBlank(message = "Name is a required field")
    @Size(min = 3, max = 50)
    private String name;

    @Size(min = 3, max = 50)
    private String icon;
}
