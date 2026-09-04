package com.spendwise.dto.response.expense;

import com.spendwise.enums.ExpenseStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class ExpenseResponse {
    private String title;
    private BigDecimal amount;
    private String categoryName;
    private String walletName;
    private ExpenseStatus status;
    private OffsetDateTime expenseAt;
}
