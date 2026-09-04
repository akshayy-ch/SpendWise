package com.spendwise.dto.response.settlement;

import com.spendwise.enums.SettlementStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SettlementResponse {

    private UUID id;
    private UUID expenseShareId;

    private String payerName;
    private String receiverName;

    private BigDecimal amount;
    private OffsetDateTime settledAt;

    private SettlementStatus status;
}