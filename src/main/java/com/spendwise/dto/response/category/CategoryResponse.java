package com.spendwise.dto.response.category;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {
    private String name;
    private String icon;
    private boolean isSystem;
}
