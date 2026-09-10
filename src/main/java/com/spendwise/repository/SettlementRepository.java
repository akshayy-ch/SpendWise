package com.spendwise.repository;

import com.spendwise.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import java.util.UUID;

public interface SettlementRepository extends JpaRepository<Settlement, UUID> {

    List<Settlement> findAllByExpenseShareId(UUID expenseShareId);

    List<Settlement> findAllByPayerIdOrReceiverId(
            UUID payerId,
            UUID receiverId
    );

    boolean existsByCategoryId(UUID categoryId);

    @Query("""
    SELECT COALESCE(SUM(s.amount), 0)
    FROM Settlement s
    WHERE s.payer.id = :userId
      AND s.settledAt >= :start
      AND s.settledAt < :end
""")
    BigDecimal sumSettlementsPaidForPeriod(
            @Param("userId") UUID userId,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end
    );

    @Query("""
    SELECT COALESCE(SUM(s.amount), 0)
    FROM Settlement s
    WHERE s.receiver.id = :userId
      AND s.settledAt >= :start
      AND s.settledAt < :end
      AND s.expenseShare.expense.expenseAt >= :start
      AND s.expenseShare.expense.expenseAt < :end
""")
    BigDecimal sumSettlementsReceivedForPeriod(
            @Param("userId") UUID userId,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end
    );

    @Query("""
    SELECT s
    FROM Settlement s
    WHERE s.payer.id = :userId
      AND s.settledAt >= :start
      AND s.settledAt < :end
      AND s.status =
          com.spendwise.enums.SettlementStatus.SETTLED
""")
    List<Settlement> findSettlementsPaidByUserForPeriod(
            @Param("userId") UUID userId,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end
    );

    @Query("""
    SELECT s
    FROM Settlement s
    WHERE (s.payer.id = :userId OR s.receiver.id = :userId)
      AND s.settledAt >= :start
      AND s.settledAt < :end
""")
    List<Settlement> findSettlementsForActivity(
            @Param("userId") UUID userId,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end
    );
}
