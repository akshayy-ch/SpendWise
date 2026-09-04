package com.spendwise.dto.response.group;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class GroupResponse {
    private String name;
    private String status;
    private String creatorName;
}
