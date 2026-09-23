package com.spendwise.service;

import com.spendwise.dto.request.expense.CreateExpenseRequest;
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
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ExpenseServiceConcurrencyTest {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private User user;
    private Wallet wallet;
    private Category category;
    private Expense expense;

    @BeforeEach
    void setUp() {
        String uniqueId = UUID.randomUUID().toString();

        user = User.builder()
                .username("concurrency_test_" + uniqueId)
                .name("Concurrency Test")
                .email("concurrency_" + uniqueId + "@test.com")
                .build();

        user.updatePasswordHash("test-password");
        user = userRepository.save(user);

        wallet = Wallet.builder()
                .walletName("Concurrency Test Wallet")
                .type(WalletType.CASH)
                .currentBalance(new BigDecimal("100.00"))
                .status(WalletStatus.ACTIVE)
                .user(user)
                .build();

        wallet = walletRepository.save(wallet);

        category = categoryRepository
                .findByNameAndIsSystemTrue("Food")
                .orElseThrow();

        expense = Expense.builder()
                .status(ExpenseStatus.ACTIVE)
                .title("Concurrency Test Expense")
                .amount(new BigDecimal("40.00"))
                .expenseAt(OffsetDateTime.now())
                .user(user)
                .wallet(wallet)
                .category(category)
                .build();

        expense = expenseRepository.save(expense);

        SpendWiseUserDetails userDetails = new SpendWiseUserDetails(user);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    private void setAuthentication() {
        SpendWiseUserDetails userDetails = new SpendWiseUserDetails(user);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void concurrentExpenseCreation_shouldNotOverspendWallet() throws Exception {

        /*
         * Our normal @BeforeEach creates the wallet with ₹60
         * because that setup is designed for the void-expense test.
         *
         * For this test we need ₹100 available.
         */
        wallet.setCurrentBalance(new BigDecimal("100.00"));
        wallet = walletRepository.save(wallet);

        CreateExpenseRequest firstRequest = CreateExpenseRequest.builder()
                .title("Concurrent Expense 1")
                .description("Concurrency test")
                .amount(new BigDecimal("70.00"))
                .expenseAt(OffsetDateTime.now())
                .walletName(wallet.getWalletName())
                .categoryName(category.getName())
                .status(ExpenseStatus.ACTIVE.name())
                .build();

        CreateExpenseRequest secondRequest = CreateExpenseRequest.builder()
                .title("Concurrent Expense 2")
                .description("Concurrency test")
                .amount(new BigDecimal("70.00"))
                .expenseAt(OffsetDateTime.now())
                .walletName(wallet.getWalletName())
                .categoryName(category.getName())
                .status(ExpenseStatus.ACTIVE.name())
                .build();

        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch startLatch = new CountDownLatch(1);

        Future<?> first = executor.submit(() -> {
            try {
                startLatch.await();
                setAuthentication();

                return expenseService.createExpense(firstRequest);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        Future<?> second = executor.submit(() -> {
            try {
                startLatch.await();
                setAuthentication();

                return expenseService.createExpense(secondRequest);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // Release both threads at approximately the same time.
        startLatch.countDown();

        executor.shutdown();

        int successCount = 0;
        int failureCount = 0;

        try {
            first.get();
            successCount++;
        } catch (ExecutionException e) {
            failureCount++;
        }

        try {
            second.get();
            successCount++;
        } catch (ExecutionException e) {
            failureCount++;
        }

        /*
         * Only one ₹70 expense can succeed because
         * the wallet contains only ₹100.
         */
        assertEquals(1, successCount);

        /*
         * The other transaction must fail because
         * the wallet has only ₹30 remaining.
         */
        assertEquals(1, failureCount);

        /*
         * Verify the actual database state.
         */
        Wallet finalWallet = walletRepository
                .findById(wallet.getId())
                .orElseThrow();

        assertEquals(
                new BigDecimal("30.00"),
                finalWallet.getCurrentBalance()
        );
    }
    @Test
    void contextLoads() {
    }

}