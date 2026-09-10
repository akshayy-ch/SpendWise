package com.spendwise.dto.response.category;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {
    private String name;
    private String icon;
    @JsonProperty("system")
    private boolean isSystem;
}
