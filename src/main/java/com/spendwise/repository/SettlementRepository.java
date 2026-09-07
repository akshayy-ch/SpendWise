package com.spendwise.repository;

import com.spendwise.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
