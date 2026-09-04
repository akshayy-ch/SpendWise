package com.spendwise.dto.request.wallet;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class CreateWalletRequest {

    @NotBlank(message = "Wallet name is a required field for wallet creation")
    @Size(min = 3, max = 100)
    private String walletName;

    @NotBlank(message = "Wallet type is a required field for wallet creation")
    @Size(min = 2, max = 20)
    private String type;

    @PositiveOrZero(message = "Initial balance cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Initial balance must have at most 10 integer digits and 2 decimal places")
    private BigDecimal initialBalance;
}
