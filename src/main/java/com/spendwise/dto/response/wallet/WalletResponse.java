package com.spendwise.dto.response.wallet;

import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class WalletResponse {
    private String walletName;
    private String username;
    private String type;
    private BigDecimal currentBalance;
    private String status;
    private OffsetDateTime createdAt;
}
