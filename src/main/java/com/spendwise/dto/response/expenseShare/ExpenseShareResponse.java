package com.spendwise.dto.response.expenseShare;

import com.spendwise.enums.ExpenseShareStatus;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExpenseShareResponse {

    private UUID id;
    private UUID expenseId;
    private String expenseTitle;
    private BigDecimal originalAmount;
    private BigDecimal remainingAmount;
    private ExpenseShareStatus status;
    private UUID groupId;
    private BigDecimal percentage;
}
