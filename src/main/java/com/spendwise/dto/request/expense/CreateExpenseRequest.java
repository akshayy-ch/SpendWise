package com.spendwise.dto.request.expense;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class CreateExpenseRequest {

    @NotBlank(message = "Title is a required field for expense creation")
    @Size(min = 3, max = 100)
    private String title;

    @Size(min = 3, max = 255)
    private String description;

    @NotNull(message = "Amount is a required field")
    @Positive(message = "Amount can't be negative")
    @Digits(integer = 10, fraction = 2, message = "Amount must have at most 10 integer digits and 2 decimal places")
    private BigDecimal amount;

    @NotNull(message = "Expense time is a required field")
    private OffsetDateTime expenseAt;

    @NotBlank(message = "Wallet Name is a required field")
    @Size(min = 3, max = 50)
    private String walletName;

    @NotBlank(message = "Category is a required field")
    @Size(min = 3, max = 50)
    private String categoryName;

    @NotBlank(message = "Status is a required field")
    private String status;
}
