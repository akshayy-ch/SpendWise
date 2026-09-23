package com.spendwise.service;

import com.spendwise.entity.User;
import com.spendwise.entity.Wallet;
import com.spendwise.enums.WalletStatus;
import com.spendwise.enums.WalletType;
import com.spendwise.repository.UserRepository;
import com.spendwise.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class WalletDatabaseInvariantTest {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {

        String uniqueId = UUID.randomUUID().toString();

        user = User.builder()
                .username("wallet_db_" + uniqueId)
                .name("Wallet DB Test")
                .email("wallet_db_" + uniqueId + "@test.com")
                .build();

        user.updatePasswordHash("test-password");

        user = userRepository.save(user);
    }

    @Test
    void wallet_shouldRejectNegativeBalance() {

        Wallet wallet = Wallet.builder()
                .walletName("InvalidWallet-" +
                        UUID.randomUUID().toString().substring(0, 8))
                .type(WalletType.CASH)
                .currentBalance(new BigDecimal("-1.00"))
                .status(WalletStatus.ACTIVE)
                .user(user)
                .build();

        assertThrows(
                Exception.class,
                () -> walletRepository.saveAndFlush(wallet)
        );
    }
}