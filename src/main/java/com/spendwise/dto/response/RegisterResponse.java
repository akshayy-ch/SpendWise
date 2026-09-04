package com.spendwise.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterResponse {
    private UUID id;
    private String username;
    private String name;
    private String email;
    private String message;
    private String walletName;
    private BigDecimal amount;
}
