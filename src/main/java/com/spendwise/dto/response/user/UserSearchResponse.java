package com.spendwise.dto.response.user;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSearchResponse {
    private UUID id;
    private String username;
    private String name;
}