package com.spendwise.dto.response.group;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupResponse {
    private UUID id;
    private String name;
    private String description;
    private String status;
    private String creatorName;
}