package com.spendwise.dto.request.expenseShare;

import com.spendwise.enums.SplitType;
import jakarta.validation.constraints.NotEmpty;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateExpenseShareRequest {

    @NotEmpty(message = "At least one user is required")
    private List<UUID> userIds;

    @NotNull(message = "Split type is required")
    private SplitType splitType;

    private List<BigDecimal> percentages;
}