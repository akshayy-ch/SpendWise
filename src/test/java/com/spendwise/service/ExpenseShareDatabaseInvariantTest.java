package com.spendwise.service;

import com.spendwise.entity.Category;
import com.spendwise.entity.Expense;
import com.spendwise.entity.ExpenseShare;
import com.spendwise.entity.Group;
import com.spendwise.entity.GroupMember;
import com.spendwise.entity.User;
import com.spendwise.entity.Wallet;
import com.spendwise.enums.ExpenseShareStatus;
import com.spendwise.enums.ExpenseStatus;
import com.spendwise.enums.GroupMemberStatus;
import com.spendwise.enums.GroupStatus;
import com.spendwise.enums.WalletStatus;
import com.spendwise.enums.WalletType;
import com.spendwise.repository.CategoryRepository;
import com.spendwise.repository.ExpenseRepository;
import com.spendwise.repository.ExpenseShareRepository;
import com.spendwise.repository.GroupMemberRepository;
import com.spendwise.repository.GroupRepository;
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
class ExpenseShareDatabaseInvariantTest {

    @Autowired
    private ExpenseShareRepository expenseShareRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private User user;
    private Wallet wallet;
    private Category category;
    private Expense expense;
    private Group group;

    @BeforeEach
    void setUp() {

        String uniqueId = UUID.randomUUID().toString();
        String shortId = uniqueId.substring(0, 8);

        // ---------------------------------------------------------
        // User
        // ---------------------------------------------------------

        user = User.builder()
                .username("share_db_" + uniqueId)
                .name("Expense Share DB Test")
                .email("share_db_" + uniqueId + "@test.com")
                .build();

        user.updatePasswordHash("test-password");

        user = userRepository.save(user);

        // ---------------------------------------------------------
        // Wallet
        // ---------------------------------------------------------

        wallet = Wallet.builder()
                .walletName("ShareDBWallet-" + shortId)
                .type(WalletType.CASH)
                .currentBalance(new BigDecimal("500.00"))
                .status(WalletStatus.ACTIVE)
                .user(user)
                .build();

        wallet = walletRepository.save(wallet);

        // ---------------------------------------------------------
        // Category
        // ---------------------------------------------------------

        category = categoryRepository
                .findByNameAndIsSystemTrue("Food")
                .orElseThrow();

        // ---------------------------------------------------------
        // Expense
        // ---------------------------------------------------------

        expense = Expense.builder()
                .status(ExpenseStatus.ACTIVE)
                .title("Share DB Expense " + uniqueId)
                .description("Expense share invariant test")
                .amount(new BigDecimal("100.00"))
                .expenseAt(OffsetDateTime.now())
                .user(user)
                .wallet(wallet)
                .category(category)
                .build();

        expense = expenseRepository.save(expense);

        // ---------------------------------------------------------
        // Group
        // ---------------------------------------------------------

        group = Group.builder()
                .name("ShareDBGroup-" + shortId)
                .description("Expense share invariant test")
                .status(GroupStatus.ACTIVE)
                .user(user)
                .build();

        group = groupRepository.save(group);
    }

    private ExpenseShare buildShare(
            BigDecimal originalAmount,
            BigDecimal remainingAmount,
            BigDecimal percentage,
            ExpenseShareStatus status
    ) {

        return ExpenseShare.builder()
                .originalAmount(originalAmount)
                .remainingAmount(remainingAmount)
                .percentage(percentage)
                .status(status)
                .user(user)
                .expense(expense)
                .group(group)
                .build();
    }

    @Test
    void expenseShare_shouldRejectZeroOriginalAmount() {

        ExpenseShare share =
                buildShare(
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        null,
                        ExpenseShareStatus.PENDING
                );

        assertThrows(
                Exception.class,
                () -> expenseShareRepository.saveAndFlush(share)
        );
    }

    @Test
    void expenseShare_shouldRejectNegativeOriginalAmount() {

        ExpenseShare share =
                buildShare(
                        new BigDecimal("-10.00"),
                        new BigDecimal("0.00"),
                        null,
                        ExpenseShareStatus.PENDING
                );

        assertThrows(
                Exception.class,
                () -> expenseShareRepository.saveAndFlush(share)
        );
    }

    @Test
    void expenseShare_shouldRejectNegativeRemainingAmount() {

        ExpenseShare share =
                buildShare(
                        new BigDecimal("100.00"),
                        new BigDecimal("-1.00"),
                        null,
                        ExpenseShareStatus.PENDING
                );

        assertThrows(
                Exception.class,
                () -> expenseShareRepository.saveAndFlush(share)
        );
    }

    @Test
    void expenseShare_shouldRejectRemainingAmountGreaterThanOriginal() {

        ExpenseShare share =
                buildShare(
                        new BigDecimal("100.00"),
                        new BigDecimal("101.00"),
                        null,
                        ExpenseShareStatus.PENDING
                );

        assertThrows(
                Exception.class,
                () -> expenseShareRepository.saveAndFlush(share)
        );
    }

    @Test
    void expenseShare_shouldRejectInvalidPercentage() {

        ExpenseShare share =
                buildShare(
                        new BigDecimal("100.00"),
                        new BigDecimal("100.00"),
                        new BigDecimal("101.00"),
                        ExpenseShareStatus.PENDING
                );

        assertThrows(
                Exception.class,
                () -> expenseShareRepository.saveAndFlush(share)
        );
    }

    @Test
    void expenseShare_shouldRejectZeroPercentage() {

        ExpenseShare share =
                buildShare(
                        new BigDecimal("100.00"),
                        new BigDecimal("100.00"),
                        BigDecimal.ZERO,
                        ExpenseShareStatus.PENDING
                );

        assertThrows(
                Exception.class,
                () -> expenseShareRepository.saveAndFlush(share)
        );
    }

    @Test
    void expenseShare_shouldRejectNullOriginalAmount() {

        ExpenseShare share =
                buildShare(
                        null,
                        BigDecimal.ZERO,
                        null,
                        ExpenseShareStatus.PENDING
                );

        assertThrows(
                Exception.class,
                () -> expenseShareRepository.saveAndFlush(share)
        );
    }

    @Test
    void expenseShare_shouldRejectNullRemainingAmount() {

        ExpenseShare share =
                buildShare(
                        new BigDecimal("100.00"),
                        null,
                        null,
                        ExpenseShareStatus.PENDING
                );

        assertThrows(
                Exception.class,
                () -> expenseShareRepository.saveAndFlush(share)
        );
    }
}