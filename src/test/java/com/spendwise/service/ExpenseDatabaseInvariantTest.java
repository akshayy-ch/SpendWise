package com.spendwise.service;

import com.spendwise.entity.Category;
import com.spendwise.entity.Expense;
import com.spendwise.entity.User;
import com.spendwise.entity.Wallet;
import com.spendwise.enums.ExpenseStatus;
import com.spendwise.enums.WalletStatus;
import com.spendwise.enums.WalletType;
import com.spendwise.repository.CategoryRepository;
import com.spendwise.repository.ExpenseRepository;
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
class ExpenseDatabaseInvariantTest {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private User user;
    private Wallet wallet;
    private Category category;

    @BeforeEach
    void setUp() {

        String uniqueId = UUID.randomUUID().toString();
        String shortId = uniqueId.substring(0, 8);

        // ---------------------------------------------------------
        // Create test user
        // ---------------------------------------------------------

        user = User.builder()
                .username("expense_db_" + uniqueId)
                .name("Expense DB Test")
                .email("expense_db_" + uniqueId + "@test.com")
                .build();

        user.updatePasswordHash("test-password");

        user = userRepository.save(user);

        // ---------------------------------------------------------
        // Create wallet
        // ---------------------------------------------------------

        wallet = Wallet.builder()
                .walletName("ExpenseDBWallet-" + shortId)
                .type(WalletType.CASH)
                .currentBalance(new BigDecimal("500.00"))
                .status(WalletStatus.ACTIVE)
                .user(user)
                .build();

        wallet = walletRepository.save(wallet);

        // ---------------------------------------------------------
        // Get system category
        // ---------------------------------------------------------

        category = categoryRepository
                .findByNameAndIsSystemTrue("Food")
                .orElseThrow();
    }

    private Expense buildExpense(
            BigDecimal amount,
            ExpenseStatus status
    ) {

        return Expense.builder()
                .status(status)
                .title("DB Invariant Expense " + UUID.randomUUID())
                .description("Database invariant test")
                .amount(amount)
                .expenseAt(OffsetDateTime.now())
                .user(user)
                .wallet(wallet)
                .category(category)
                .build();
    }

    @Test
    void expense_shouldRejectZeroAmount() {

        Expense expense =
                buildExpense(
                        BigDecimal.ZERO,
                        ExpenseStatus.ACTIVE
                );

        /*
         * PostgreSQL constraint:
         *
         * amount > 0
         */

        assertThrows(
                Exception.class,
                () -> expenseRepository.saveAndFlush(expense)
        );
    }

    @Test
    void expense_shouldRejectNegativeAmount() {

        Expense expense =
                buildExpense(
                        new BigDecimal("-10.00"),
                        ExpenseStatus.ACTIVE
                );

        /*
         * PostgreSQL constraint:
         *
         * amount > 0
         */

        assertThrows(
                Exception.class,
                () -> expenseRepository.saveAndFlush(expense)
        );
    }

    @Test
    void expense_shouldRejectInvalidStatus() {

        Expense expense =
                buildExpense(
                        new BigDecimal("50.00"),
                        null
                );

        /*
         * We cannot use an invalid Java enum value because
         * ExpenseStatus itself only contains valid application
         * values.
         *
         * Therefore this test uses the entity's status field as
         * null. PostgreSQL should reject it because status is
         * NOT NULL.
         */

        assertThrows(
                Exception.class,
                () -> expenseRepository.saveAndFlush(expense)
        );
    }
}