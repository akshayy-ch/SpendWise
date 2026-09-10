package com.spendwise.service;

import com.spendwise.entity.*;
import com.spendwise.repository.*;
import lombok.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final ExpenseRepository expenseRepository;
    private final SettlementRepository settlementRepository;
    private final CurrentUserService currentUserService;
    private final BudgetPeriodRepository budgetPeriodRepository;
    private final ExpenseShareRepository expenseShareRepository;
    private final IncomeRepository incomeRepository;


    public BigDecimal getSpentThisMonth(){
        UUID userId = currentUserService.getCurrentUserId();

        LocalDate dateInMonth = LocalDate.now();

        ZoneId zone = ZoneId.systemDefault();
        OffsetDateTime start = dateInMonth.with(TemporalAdjusters.firstDayOfMonth())
                .atStartOfDay(zone)
                .toOffsetDateTime();


        OffsetDateTime end = dateInMonth.with(TemporalAdjusters.firstDayOfNextMonth())
                .atStartOfDay(zone)
                .toOffsetDateTime();


        BigDecimal spent = expenseRepository.sumActiveExpensesForPeriod(userId, start, end);
        spent = spent.add(settlementRepository.sumSettlementsPaidForPeriod(userId, start, end));
        spent = spent.subtract(settlementRepository.sumSettlementsReceivedForPeriod(userId, start, end));

        return spent;
    }

    public BigDecimal getBudgetRemaining(){
        UUID userId = currentUserService.getCurrentUserId();

        BudgetPeriod budgetPeriod = budgetPeriodRepository
                .findByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        userId,
                        LocalDate.now(),
                        LocalDate.now()
                )
                .orElse(null);

        if (budgetPeriod == null) {
            return null;
        }
        List<Budget> budgetList = budgetPeriod.getBudgets();
        Budget overallBudget = budgetList.stream()
                .filter(budget -> budget.getCategory() == null)
                .findFirst()
                .orElse(null);

        if (overallBudget == null) {
            return null;
        }

        ZoneId zone = ZoneId.systemDefault();
        OffsetDateTime start = budgetPeriod.getStartDate().atStartOfDay(zone)
                .toOffsetDateTime();

        OffsetDateTime end = budgetPeriod.getEndDate().plusDays(1)
                .atStartOfDay(zone)
                .toOffsetDateTime();


        BigDecimal spent = expenseRepository.sumActiveExpensesForPeriod(userId, start, end);
        spent = spent.add(settlementRepository.sumSettlementsPaidForPeriod(userId, start, end));
        spent = spent.subtract(settlementRepository.sumSettlementsReceivedForPeriod(userId, start, end));

        return overallBudget.getBudgetLimit().subtract(spent);
    }

    public BigDecimal getYouOwe(){
        UUID userId = currentUserService.getCurrentUserId();

        BigDecimal youOwe = expenseShareRepository.sumOutstandingAmountByUserId(userId);
        return youOwe;
    }

    public BigDecimal getYouAreOwed(){
        UUID userId = currentUserService.getCurrentUserId();

        LocalDate dateInMonth = LocalDate.now();

        ZoneId zone = ZoneId.systemDefault();
        OffsetDateTime start = dateInMonth.with(TemporalAdjusters.firstDayOfMonth())
                .atStartOfDay(zone)
                .toOffsetDateTime();


        OffsetDateTime end = dateInMonth.with(TemporalAdjusters.firstDayOfNextMonth())
                .atStartOfDay(zone)
                .toOffsetDateTime();

        return  expenseShareRepository.sumAmountOwedToUserForPeriod(userId, start, end);
    }

    public List<CategorySpendingResponse> getSpendingByCategory() {

        UUID userId = currentUserService.getCurrentUserId();

        BudgetPeriod budgetPeriod = budgetPeriodRepository
                .findByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        userId,
                        LocalDate.now(),
                        LocalDate.now()
                )
                .orElse(null);

        if (budgetPeriod == null) {
            return List.of();
        }

        Map<String, BigDecimal> categoryBudgets = new HashMap<>();

        List<Budget> budgets = budgetPeriod.getBudgets();

        for (Budget budget : budgets) {

            if (budget.getCategory() != null) {
                categoryBudgets.put(
                        budget.getCategory().getName(),
                        budget.getBudgetLimit()
                );
            }
        }

        ZoneId zone = ZoneId.systemDefault();

        OffsetDateTime start = budgetPeriod.getStartDate()
                .atStartOfDay(zone)
                .toOffsetDateTime();

        OffsetDateTime end = budgetPeriod.getEndDate()
                .plusDays(1)
                .atStartOfDay(zone)
                .toOffsetDateTime();

        Map<String, BigDecimal> categorySpending = new HashMap<>();

        List<Expense> periodExpenses =
                expenseRepository.findActiveExpensesForPeriod(
                        userId, start, end
                );

        for (Expense expense : periodExpenses) {

            String category = expense.getCategory().getName();

            BigDecimal effectiveAmount =
                    expense.getAmount()
                            .subtract(getSettledShareAmount(expense));

            categorySpending.merge(
                    category,
                    effectiveAmount,
                    BigDecimal::add
            );
        }

        List<Settlement> settlements =
                settlementRepository.findSettlementsPaidByUserForPeriod(
                        userId, start, end
                );

        for (Settlement settlement : settlements) {

            categorySpending.merge(
                    settlement.getCategory().getName(),
                    settlement.getAmount(),
                    BigDecimal::add
            );
        }

        List<CategorySpendingResponse> response = new ArrayList<>();

        for (Map.Entry<String, BigDecimal> entry : categoryBudgets.entrySet()) {

            String category = entry.getKey();
            BigDecimal budget = entry.getValue();

            BigDecimal spent = categorySpending.getOrDefault(
                    category,
                    BigDecimal.ZERO
            );

            CategorySpendingResponse temp =
                    CategorySpendingResponse.builder()
                            .categoryName(category)
                            .spent(spent)
                            .budget(budget)
                            .build();

            response.add(temp);
        }
        return response;
    }

    private BigDecimal getSettledShareAmount(Expense expense) {

        List<ExpenseShare> shares = expenseShareRepository.findAllByExpenseId(expense.getId());

        BigDecimal settledAmount = BigDecimal.ZERO;

        for (ExpenseShare share : shares) {
            settledAmount = settledAmount.add(
                    share.getOriginalAmount().subtract(share.getRemainingAmount())
            );
        }

        return settledAmount;
    }

    public List<OpenGroupResponse> getOpenGroups() {

        UUID userId = currentUserService.getCurrentUserId();

        List<Object[]> openGroups =
                expenseShareRepository.findOpenGroupsForUser(userId);

        List<OpenGroupResponse> response = new ArrayList<>();

        for (Object[] row : openGroups) {

            OpenGroupResponse temp = OpenGroupResponse.builder()
                    .groupId((UUID) row[0])
                    .groupName((String) row[1])
                    .openExpenseCount((Long) row[2])
                    .build();

            response.add(temp);
        }

        return response;
    }

    public List<RecentActivityResponse> getRecentActivity() {

        UUID userId = currentUserService.getCurrentUserId();

        OffsetDateTime end = OffsetDateTime.now();
        OffsetDateTime start = end.minusDays(7);

        List<RecentActivityResponse> response = new ArrayList<>();

        List<Expense> expenses = expenseRepository.findExpensesForActivity(userId, start, end);

        for (Expense expense : expenses) {
            response.add(
                    RecentActivityResponse.builder()
                            .id(expense.getId())
                            .type("EXPENSE")
                            .title(expense.getTitle())
                            .amount(expense.getAmount().negate())
                            .occurredAt(expense.getExpenseAt())
                            .build()
            );
        }
        List<Income> incomes = incomeRepository.findIncomeForActivity(userId, start, end);

        for (Income income : incomes) {
            response.add(
                    RecentActivityResponse.builder()
                            .id(income.getId())
                            .type("INCOME")
                            .title(income.getSource())
                            .amount(income.getAmount())
                            .occurredAt(income.getIncomeAt())
                            .build()
            );
        }

        List<Settlement> settlements = settlementRepository.findSettlementsForActivity(userId, start, end);

        for (Settlement settlement : settlements) {
            BigDecimal amount;
            if (settlement.getPayer().getId().equals(userId)) {
                amount = settlement.getAmount().negate();
            } else {
                amount = settlement.getAmount();
            }
            response.add(
                    RecentActivityResponse.builder()
                            .id(settlement.getId())
                            .type("SETTLEMENT")
                            .title("Settlement")
                            .amount(amount)
                            .occurredAt(settlement.getSettledAt())
                            .build()
            );
        }
        response.sort(Comparator.comparing(RecentActivityResponse::getOccurredAt).reversed());

        return response;
    }

    @Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
    public static class CategorySpendingResponse {

        private String categoryName;
        private BigDecimal spent;
        private BigDecimal budget;
    }
    @Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
    public static class OpenGroupResponse {

        private UUID groupId;
        private String groupName;
        private long openExpenseCount;
    }
    @Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
    public static class RecentActivityResponse {

        private UUID id;
        private String type;
        private String title;
        private BigDecimal amount;
        private OffsetDateTime occurredAt;
    }
}