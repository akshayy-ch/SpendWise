package com.spendwise.repository;

import com.spendwise.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    List<Wallet> findAllByUserId(UUID userId);

    Optional<Wallet> findByWalletNameAndUserId(String walletName, UUID userId);

    boolean existsByWalletNameAndUserId(String walletName, UUID userId);

    boolean existsByWalletNameAndUserIdAndIdNot(
            String walletName,
            UUID userId,
            UUID walletId
    );
}
