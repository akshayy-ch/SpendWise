package com.spendwise.repository;

import com.spendwise.entity.ExpenseShare;
import com.spendwise.enums.ExpenseShareStatus;
import com.spendwise.repository.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
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
}