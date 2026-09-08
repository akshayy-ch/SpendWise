package com.spendwise.dto.request.budget;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class createBudgetRequest {

    @Positive(message = "Overall budget can't be negative")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal overallBudget;

    @Valid
    private List<CategoryBudgetItem> categoryBudgets;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
    public static class CategoryBudgetItem {
        @NotBlank
        private String categoryName;

        @Positive
        @Digits(integer = 10, fraction = 2)
        private BigDecimal limit;
    }
}