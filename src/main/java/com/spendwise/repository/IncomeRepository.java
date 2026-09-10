package com.spendwise.repository;

import com.spendwise.entity.Expense;
import com.spendwise.entity.Income;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IncomeRepository extends JpaRepository<Income, UUID>, JpaSpecificationExecutor<Income> {

    List<Income> findByUserId(UUID userId);

    Optional<Income> findByIdAndUserId(UUID incomeId, UUID userId);

    List<Income> findByUserIdAndSource(
            UUID userId,
            String source
    );

    List<Income> findByUserIdAndIncomeAtBetween(
            UUID userId,
            OffsetDateTime start,
            OffsetDateTime end
    );

    List<Income> findByUserIdAndIncomeAtGreaterThanEqual(
            UUID userId,
            OffsetDateTime start
    );

    List<Income> findByUserIdAndIncomeAtLessThanEqual(
            UUID userId,
            OffsetDateTime end
    );
    List<Income> findByUserIdAndSourceAndIncomeAtBetween(
            UUID userId,
            String source,
            OffsetDateTime start,
            OffsetDateTime end
    );

    @Query("""
    SELECT i
    FROM Income i
    WHERE i.user.id = :userId
      AND i.incomeAt >= :start
      AND i.incomeAt < :end
""")
    List<Income> findIncomeForActivity(
            @Param("userId") UUID userId,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end
    );
}
