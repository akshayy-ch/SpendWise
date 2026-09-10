package com.spendwise.repository;

import com.spendwise.entity.ExpenseShare;
import com.spendwise.enums.ExpenseShareStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExpenseShareRepository
        extends JpaRepository<ExpenseShare, UUID> {

    Optional<ExpenseShare> findByExpenseIdAndUserId(
            UUID expenseId,
            UUID userId
    );

    List<ExpenseShare> findAllByExpenseId(
            UUID expenseId
    );

    List<ExpenseShare> findAllByUserId(
            UUID userId
    );

    List<ExpenseShare> findAllByUserIdAndStatus(
            UUID userId,
            ExpenseShareStatus status
    );

    @Query("""
    SELECT CASE WHEN COUNT(es) > 0 THEN true ELSE false END
    FROM ExpenseShare es
    WHERE es.group.id = :groupId
      AND es.user.id = :userId
      AND es.remainingAmount > 0
      AND es.expense.status =
          com.spendwise.enums.ExpenseStatus.ACTIVE
""")
    boolean hasOutstandingBalance(
            @Param("groupId") UUID groupId,
            @Param("userId") UUID userId
    );

    @Query("""
        SELECT CASE WHEN COUNT(es) > 0 THEN true ELSE false END
        FROM ExpenseShare es
        WHERE es.group.id = :groupId
          AND es.remainingAmount > 0
          AND es.expense.status =
              com.spendwise.enums.ExpenseStatus.ACTIVE
    """)
    boolean hasOutstandingShares(
            @Param("groupId") UUID groupId
    );
    List<ExpenseShare> findByExpenseId(UUID expenseId);
    List<ExpenseShare> findByUserId(UUID userId);

    @Query("""
    SELECT COALESCE(SUM(es.remainingAmount), 0)
    FROM ExpenseShare es
    WHERE es.user.id = :userId
      AND es.remainingAmount > 0
      AND es.expense.status =
          com.spendwise.enums.ExpenseStatus.ACTIVE
""")
    BigDecimal sumOutstandingAmountByUserId(
            @Param("userId") UUID userId
    );

    @Query("""
    SELECT COALESCE(SUM(es.remainingAmount), 0)
    FROM ExpenseShare es
    WHERE es.expense.user.id = :userId
      AND es.expense.expenseAt >= :start
      AND es.expense.expenseAt < :end
      AND es.remainingAmount > 0
      AND es.expense.status =
          com.spendwise.enums.ExpenseStatus.ACTIVE
""")
    BigDecimal sumAmountOwedToUserForPeriod(
            @Param("userId") UUID userId,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end
    );

    @Query("""
    SELECT es.group.id, es.group.name, COUNT(es)
    FROM ExpenseShare es
    WHERE es.user.id = :userId
      AND es.remainingAmount > 0
      AND es.expense.status =
          com.spendwise.enums.ExpenseStatus.ACTIVE
    GROUP BY es.group.id, es.group.name
""")
    List<Object[]> findOpenGroupsForUser(@Param("userId") UUID userId);
}