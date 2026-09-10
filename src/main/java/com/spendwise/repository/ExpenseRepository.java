package com.spendwise.repository;

import com.spendwise.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID>, JpaSpecificationExecutor<Expense> {

    Optional<Expense> findByIdAndUserId(UUID expenseId, UUID userId);

    List<Expense> findByUserIdAndCategoryId(
            UUID userId,
            UUID categoryId
    );

    List<Expense> findByUserIdAndWalletId(
            UUID userId,
            UUID walletId
    );

    List<Expense> findByUserIdAndExpenseAtBetween(
            UUID userId,
            OffsetDateTime start,
            OffsetDateTime end
    );

    List<Expense> findByUserIdAndCategoryIdAndExpenseAtBetween(
            UUID userId, UUID categoryID, OffsetDateTime start,
            OffsetDateTime end
    );

    List<Expense> findByUserIdAndExpenseAtGreaterThanEqual(UUID userId, OffsetDateTime start);

    List<Expense> findByUserIdAndExpenseAtLessThanEqual(UUID userId, OffsetDateTime end);

    boolean existsByCategoryNameAndUserId(String categoryName, UUID userId);

    boolean existsByCategoryIdAndUserId(UUID categoryId, UUID userId);

    @Query("""
    SELECT COALESCE(SUM(e.amount), 0)
    FROM Expense e
    WHERE e.user.id = :userId
      AND e.expenseAt >= :start
      AND e.expenseAt < :end
      AND e.status = ExpenseStatus.ACTIVE
    """)
    BigDecimal sumActiveExpensesForPeriod(
            @Param("userId") UUID userId,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end
    );

    @Query("""
    SELECT e
    FROM Expense e
    WHERE e.user.id = :userId
      AND e.expenseAt >= :start
      AND e.expenseAt < :end
      AND e.status = com.spendwise.enums.ExpenseStatus.ACTIVE
""")
    List<Expense> findActiveExpensesForPeriod(
            @Param("userId") UUID userId,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end
    );

    @Query("""
    SELECT e
    FROM Expense e
    WHERE e.user.id = :userId
      AND e.expenseAt >= :start
      AND e.expenseAt < :end
""")
    List<Expense> findExpensesForActivity(
            @Param("userId") UUID userId,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end
    );
}