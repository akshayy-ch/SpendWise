package com.spendwise.repository;

import com.spendwise.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID>, JpaSpecificationExecutor<Expense> {

    boolean existsByCategoryIdAndUserId(UUID categoryId, UUID userId);
    boolean existsByCategoryId(UUID categoryId);

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