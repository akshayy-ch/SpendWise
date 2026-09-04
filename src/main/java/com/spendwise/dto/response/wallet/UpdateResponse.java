package com.spendwise.dto.response.wallet;

import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateResponse {
    private String walletName;
    private String username;
    private String type;
    private BigDecimal currentBalance;
    private OffsetDateTime createdAt;
}