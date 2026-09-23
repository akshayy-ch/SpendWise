package com.spendwise.service;

import com.spendwise.entity.Category;
import com.spendwise.entity.Expense;
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
class ExpenseVoidRollbackTest {

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
    private Expense expense;

    @BeforeEach
    void setUp() {

        String uniqueId = UUID.randomUUID().toString();
        String shortId = uniqueId.substring(0, 8);

        user = User.builder()
                .username("void_rollback_" + uniqueId)
                .name("Void Rollback Test")
                .email("void_rollback_" + uniqueId + "@test.com")
                .build();

        user.updatePasswordHash("test-password");
        user = userRepository.save(user);

        wallet = Wallet.builder()
                .walletName("VoidRollback-" + shortId)
                .type(WalletType.CASH)
                .currentBalance(new BigDecimal("120.00"))
                .status(WalletStatus.ACTIVE)
                .user(user)
                .build();

        wallet = walletRepository.save(wallet);

        category = categoryRepository
                .findByNameAndIsSystemTrue("Food")
                .orElseThrow();

        expense = Expense.builder()
                .status(ExpenseStatus.ACTIVE)
                .title("Void Rollback Expense " + uniqueId)
                .description("Rollback test")
                .amount(new BigDecimal("40.00"))
                .expenseAt(OffsetDateTime.now())
                .user(user)
                .wallet(wallet)
                .category(category)
                .build();

        expense = expenseRepository.save(expense);

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
    void voidExpenseFailure_shouldRollbackExpenseAndWallet() {

        assertEquals(
                ExpenseStatus.ACTIVE,
                expense.getStatus()
        );

        assertEquals(
                new BigDecimal("120.00"),
                wallet.getCurrentBalance()
        );

        doThrow(
                new RuntimeException(
                        "Forced failure for void rollback test"
                )
        )
                .when(expenseMapper)
                .toResponse(any(Expense.class));

        assertThrows(
                RuntimeException.class,
                () -> expenseService.voidExpense(expense.getId())
        );

        Expense finalExpense =
                expenseRepository
                        .findById(expense.getId())
                        .orElseThrow();

        Wallet finalWallet =
                walletRepository
                        .findById(wallet.getId())
                        .orElseThrow();

        assertEquals(
                ExpenseStatus.ACTIVE,
                finalExpense.getStatus()
        );

        assertEquals(
                new BigDecimal("120.00"),
                finalWallet.getCurrentBalance()
        );
    }
}