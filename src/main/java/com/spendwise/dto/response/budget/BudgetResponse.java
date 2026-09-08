package com.spendwise.dto.response.budget;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BudgetResponse {

    private UUID id;

    private BigDecimal overallBudget;

    private List<CategoryBudgetItem> categoryBudgets;

    private LocalDate startDate;

    private LocalDate endDate;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class CategoryBudgetItem {

        private String categoryName;

        private BigDecimal limit;
    }
}