package com.spendwise.dto.request.category;

import jakarta.validation.constraints.NotBlank;
import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class GetCategoriesRequest {

    @NotBlank(message = "Filter is a required field")
    private String filter;
}
