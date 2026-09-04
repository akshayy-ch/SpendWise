package com.spendwise.dto.response.income;

import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class IncomeResponse {
    private String source;
    private String walletName;
    private BigDecimal amount;
    private OffsetDateTime incomeAt;
}
