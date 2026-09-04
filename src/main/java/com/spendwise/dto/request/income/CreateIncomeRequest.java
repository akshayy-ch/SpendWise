package com.spendwise.dto.request.income;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class CreateIncomeRequest {

    @NotBlank(message = "Source is a required field")
    @Size(min = 3, max = 50)
    private String source;

    @NotBlank(message = "WalletName is a required field")
    @Size(min = 3, max = 50)
    private String walletName;

    @Size(min = 3, max = 255)
    private String description;

    @NotNull(message = "Amount is a required field")
    @Positive(message = "Amount must be greated than zero")
    @Digits(integer = 10, fraction = 2, message = "Amount must have at most 10 integer digits and 2 decimal places")
    private BigDecimal amount;

    @NotNull(message = "Income time is a required field")
    private OffsetDateTime incomeAt;
}
