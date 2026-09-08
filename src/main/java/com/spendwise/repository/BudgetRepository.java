package com.spendwise.repository;

import com.spendwise.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {

    List<Budget> findByBudgetPeriodId(UUID budgetPeriodId);

    boolean existsByBudgetPeriodIdAndCategoryIsNull(UUID budgetPeriodId);

    boolean existsByBudgetPeriodIdAndCategoryId(
            UUID budgetPeriodId,
            UUID categoryId
    );
}