package com.spendwise.service;

import com.spendwise.entity.Income;
import com.spendwise.entity.User;
import com.spendwise.entity.Wallet;
import com.spendwise.enums.WalletStatus;
import com.spendwise.enums.WalletType;
import com.spendwise.repository.IncomeRepository;
import com.spendwise.repository.UserRepository;
import com.spendwise.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class IncomeDatabaseInvariantTest {

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    private User user;
    private Wallet wallet;

    @BeforeEach
    void setUp() {

        String uniqueId = UUID.randomUUID().toString();
        String shortId = uniqueId.substring(0, 8);

        // ---------------------------------------------------------
        // Create test user
        // ---------------------------------------------------------

        user = User.builder()
                .username("income_db_" + uniqueId)
                .name("Income DB Test")
                .email("income_db_" + uniqueId + "@test.com")
                .build();

        user.updatePasswordHash("test-password");

        user = userRepository.save(user);

        // ---------------------------------------------------------
        // Create wallet
        // ---------------------------------------------------------

        wallet = Wallet.builder()
                .walletName("IncomeDBWallet-" + shortId)
                .type(WalletType.CASH)
                .currentBalance(new BigDecimal("500.00"))
                .status(WalletStatus.ACTIVE)
                .user(user)
                .build();

        wallet = walletRepository.save(wallet);
    }

    private Income buildIncome(BigDecimal amount) {

        return Income.builder()
                .source("Database Test")
                .description("Income database invariant test")
                .amount(amount)
                .incomeAt(OffsetDateTime.now())
                .user(user)
                .wallet(wallet)
                .build();
    }

    @Test
    void income_shouldRejectZeroAmount() {

        Income income =
                buildIncome(BigDecimal.ZERO);

        /*
         * PostgreSQL constraint:
         *
         * amount > 0
         */

        assertThrows(
                Exception.class,
                () -> incomeRepository.saveAndFlush(income)
        );
    }

    @Test
    void income_shouldRejectNegativeAmount() {

        Income income =
                buildIncome(new BigDecimal("-10.00"));

        /*
         * PostgreSQL constraint:
         *
         * amount > 0
         */

        assertThrows(
                Exception.class,
                () -> incomeRepository.saveAndFlush(income)
        );
    }

    @Test
    void income_shouldRejectNullAmount() {

        Income income =
                buildIncome(null);

        /*
         * PostgreSQL constraint:
         *
         * amount IS NOT NULL
         */

        assertThrows(
                Exception.class,
                () -> incomeRepository.saveAndFlush(income)
        );
    }
}