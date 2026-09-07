package com.spendwise.dto.request.settlement;

import com.spendwise.entity.User;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class CreateSettlementRequest {

    @NotNull(message = "Amount is a required field")
    @Positive(message = "Amount must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "Amount must have at most 10 integer digits and 2 decimal places")
    private BigDecimal amount;

    @NotNull(message = "Receiver id is a required field")
    private UUID receiverId;

    @NotBlank(message = "Category is a required field")
    @Size(min = 3, max = 50)
    private String categoryName;

}
