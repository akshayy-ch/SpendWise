package com.spendwise.service;

import com.spendwise.dto.request.expense.CreateExpenseRequest;
import com.spendwise.dto.request.income.CreateIncomeRequest;
import com.spendwise.entity.Category;
import com.spendwise.entity.User;
import com.spendwise.entity.Wallet;
import com.spendwise.enums.ExpenseStatus;
import com.spendwise.enums.WalletStatus;
import com.spendwise.enums.WalletType;
import com.spendwise.repository.CategoryRepository;
import com.spendwise.repository.ExpenseRepository;
import com.spendwise.repository.IncomeRepository;
import com.spendwise.repository.UserRepository;
import com.spendwise.repository.WalletRepository;
import com.spendwise.security.SpendWiseUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class IncomeExpenseConcurrencyTest {

    @Autowired
    private IncomeService incomeService;

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

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
                .username("income_expense_" + uniqueId)
                .name("Income Expense Concurrency Test")
                .email("income_expense_" + uniqueId + "@test.com")
                .build();

        user.updatePasswordHash("test-password");

        user = userRepository.save(user);

        // ---------------------------------------------------------
        // Create wallet
        //
        // Initial balance = ₹100
        // ---------------------------------------------------------

        wallet = Wallet.builder()
                .walletName("RaceWallet-" + shortId)
                .type(WalletType.CASH)
                .currentBalance(new BigDecimal("100.00"))
                .status(WalletStatus.ACTIVE)
                .user(user)
                .build();

        wallet = walletRepository.save(wallet);

        // ---------------------------------------------------------
        // Get system category for the expense
        // ---------------------------------------------------------

        category = categoryRepository
                .findByNameAndIsSystemTrue("Food")
                .orElseThrow();
    }

    private void setAuthentication() {

        SpendWiseUserDetails userDetails =
                new SpendWiseUserDetails(user);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);
    }

    @Test
    void concurrentIncomeAndExpense_shouldPreserveFinalWalletBalance()
            throws Exception {

        String walletName = wallet.getWalletName();

        // ---------------------------------------------------------
        // Income request
        //
        // +₹70
        // ---------------------------------------------------------

        CreateIncomeRequest incomeRequest =
                CreateIncomeRequest.builder()
                        .source("Salary")
                        .walletName(walletName)
                        .description("Concurrency income test")
                        .amount(new BigDecimal("70.00"))
                        .incomeAt(OffsetDateTime.now())
                        .build();

        // ---------------------------------------------------------
        // Expense request
        //
        // -₹80
        // ---------------------------------------------------------

        CreateExpenseRequest expenseRequest =
                CreateExpenseRequest.builder()
                        .title("Concurrency Expense")
                        .description("Concurrency expense test")
                        .amount(new BigDecimal("80.00"))
                        .expenseAt(OffsetDateTime.now())
                        .walletName(walletName)
                        .categoryName(category.getName())
                        .status(ExpenseStatus.ACTIVE.name())
                        .build();

        /*
         * Both transactions must start around the same time.
         */
        CountDownLatch startLatch =
                new CountDownLatch(1);

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        Future<?> incomeFuture =
                executor.submit(() -> {

                    try {
                        setAuthentication();

                        startLatch.await();

                        incomeService.createIncome(incomeRequest);

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

        Future<?> expenseFuture =
                executor.submit(() -> {

                    try {
                        setAuthentication();

                        startLatch.await();

                        expenseService.createExpense(expenseRequest);

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

        /*
         * Release both threads.
         */
        startLatch.countDown();

        /*
         * Wait for both transactions to finish.
         */
        incomeFuture.get();
        expenseFuture.get();

        executor.shutdown();

        // ---------------------------------------------------------
        // Reload wallet from database
        // ---------------------------------------------------------

        Wallet finalWallet =
                walletRepository
                        .findById(wallet.getId())
                        .orElseThrow();

        /*
         * Expected:
         *
         * Initial     = 100
         * Income      = +70
         * Expense     = -80
         * ----------------
         * Final       = 90
         */

        assertEquals(
                new BigDecimal("90.00"),
                finalWallet.getCurrentBalance()
        );

        // ---------------------------------------------------------
        // Verify both transactions actually persisted
        // ---------------------------------------------------------

        long incomeCount =
                incomeRepository
                        .findByUserId(user.getId())
                        .stream()
                        .filter(income ->
                                income.getAmount()
                                        .compareTo(new BigDecimal("70.00")) == 0)
                        .count();

        long expenseCount =
                expenseRepository
                        .findAll()
                        .stream()
                        .filter(expense ->
                                expense.getUser().getId().equals(user.getId())
                                        && expense.getAmount()
                                        .compareTo(new BigDecimal("80.00")) == 0)
                        .count();

        assertEquals(1, incomeCount);
        assertEquals(1, expenseCount);

        /*
         * The final balance must never be negative.
         */
        assertTrue(
                finalWallet.getCurrentBalance()
                        .compareTo(BigDecimal.ZERO) >= 0
        );
    }
}