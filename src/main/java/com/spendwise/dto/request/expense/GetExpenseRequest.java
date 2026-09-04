package com.spendwise.dto.request.expense;

import com.spendwise.enums.ExpenseStatus;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class GetExpenseRequest {

    @Size(min = 3, max = 50)
    private String categoryName;

    @Size(min = 3, max = 100)
    private String walletName;

    private ExpenseStatus status;

    private OffsetDateTime from;

    private OffsetDateTime to;
}
