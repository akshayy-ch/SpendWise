package com.spendwise.repository;

import com.spendwise.entity.BudgetPeriod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface BudgetPeriodRepository extends JpaRepository<BudgetPeriod, UUID> {

    boolean existsByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            UUID userId,
            LocalDate startDate,
            LocalDate endDate
    );

    Optional<BudgetPeriod> findByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            UUID userId,
            LocalDate startDate,
            LocalDate endDate
    );
}