package com.spendwise.service;

import com.spendwise.dto.request.expense.CreateExpenseRequest;
import com.spendwise.entity.Category;
import com.spendwise.entity.User;
import com.spendwise.entity.Wallet;
import com.spendwise.enums.ExpenseStatus;
import com.spendwise.enums.WalletStatus;
import com.spendwise.enums.WalletType;
import com.spendwise.mapper.ExpenseMapper;
import com.spendwise.repository.CategoryRepository;
import com.spendwise.repository.ExpenseRepository;
import com.spendwise.repository.UserRepository;
import com.spendwise.repository.WalletRepository;
import com.spendwise.security.SpendWiseUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
class ExpenseServiceRollbackTest {

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

    @SpyBean
    private ExpenseMapper expenseMapper;

    private User user;
    private Wallet wallet;
    private Category category;

    @BeforeEach
    void setUp() {

        String uniqueId = UUID.randomUUID().toString();
        String shortId = uniqueId.substring(0, 8);

        user = User.builder()
                .username("expense_rollback_" + uniqueId)
                .name("Expense Rollback Test")
                .email("expense_rollback_" + uniqueId + "@test.com")
                .build();

        user.updatePasswordHash("test-password");
        user = userRepository.save(user);

        wallet = Wallet.builder()
                .walletName("RollbackWallet-" + shortId)
                .type(WalletType.CASH)
                .currentBalance(new BigDecimal("200.00"))
                .status(WalletStatus.ACTIVE)
                .user(user)
                .build();

        wallet = walletRepository.save(wallet);

        category = categoryRepository
                .findByNameAndIsSystemTrue("Food")
                .orElseThrow();

        setAuthentication();
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
    void expenseCreationFailure_shouldRollbackWalletAndExpense() throws Exception {

        BigDecimal initialBalance =
                wallet.getCurrentBalance();

        String title =
                "Rollback Expense " + UUID.randomUUID();

        long expensesBefore =
                expenseRepository.findAll()
                        .stream()
                        .filter(expense ->
                                title.equals(expense.getTitle()))
                        .count();

        assertEquals(
                new BigDecimal("200.00"),
                initialBalance
        );

        assertEquals(0, expensesBefore);

        CreateExpenseRequest request =
                CreateExpenseRequest.builder()
                        .title(title)
                        .description("Rollback test")
                        .amount(new BigDecimal("70.00"))
                        .expenseAt(OffsetDateTime.now())
                        .walletName(wallet.getWalletName())
                        .categoryName(category.getName())
                        .status(ExpenseStatus.ACTIVE.name())
                        .build();

        doThrow(
                new RuntimeException(
                        "Forced failure for rollback test"
                )
        )
                .when(expenseMapper)
                .toResponse(any());

        assertThrows(
                RuntimeException.class,
                () -> expenseService.createExpense(request)
        );

        Wallet finalWallet =
                walletRepository
                        .findById(wallet.getId())
                        .orElseThrow();

        assertEquals(
                new BigDecimal("200.00"),
                finalWallet.getCurrentBalance()
        );

        long expensesAfter =
                expenseRepository.findAll()
                        .stream()
                        .filter(expense ->
                                title.equals(expense.getTitle()))
                        .count();

        assertEquals(0, expensesAfter);
    }
}