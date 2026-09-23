package com.spendwise.service;

import com.spendwise.entity.Category;
import com.spendwise.entity.Expense;
import com.spendwise.entity.ExpenseShare;
import com.spendwise.entity.Group;
import com.spendwise.entity.Settlement;
import com.spendwise.entity.User;
import com.spendwise.entity.Wallet;
import com.spendwise.enums.*;
import com.spendwise.repository.CategoryRepository;
import com.spendwise.repository.ExpenseRepository;
import com.spendwise.repository.ExpenseShareRepository;
import com.spendwise.repository.GroupRepository;
import com.spendwise.repository.SettlementRepository;
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
class SettlementDatabaseInvariantTest {

    @Autowired
    private SettlementRepository settlementRepository;

    @Autowired
    private ExpenseShareRepository expenseShareRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private CategoryRepository categoryRepository;


    private User payer;
    private User receiver;

    private Wallet wallet;
    private Category category;

    private Expense expense;
    private ExpenseShare expenseShare;
    private Group group;


    @BeforeEach
    void setUp() {

        String suffix = UUID.randomUUID()
                .toString()
                .substring(0, 8);

        /*
         * ---------------------------------------------------------
         * PAYER
         * ---------------------------------------------------------
         */
        payer = userRepository.save(
                User.builder()
                        .username("settlement-payer-" + suffix)
                        .name("Settlement Payer")
                        .email("payer-" + suffix + "@test.com")
                        .phoneNumber("9" + Math.abs(UUID.randomUUID()
                                .getMostSignificantBits() % 1_000_000_000L))
                        .passwordHash("test-password-hash")
                        .build()
        );


        /*
         * ---------------------------------------------------------
         * RECEIVER
         * ---------------------------------------------------------
         */
        receiver = userRepository.save(
                User.builder()
                        .username("settlement-receiver-" + suffix)
                        .name("Settlement Receiver")
                        .email("receiver-" + suffix + "@test.com")
                        .phoneNumber("8" + Math.abs(UUID.randomUUID()
                                .getMostSignificantBits() % 1_000_000_000L))
                        .passwordHash("test-password-hash")
                        .build()
        );


        /*
         * ---------------------------------------------------------
         * WALLET
         * ---------------------------------------------------------
         */
        wallet = walletRepository.save(
                Wallet.builder()
                        .walletName("SettlementWallet-" + suffix)
                        .type(WalletType.CASH)
                        .currentBalance(new BigDecimal("500.00"))
                        .status(WalletStatus.ACTIVE)
                        .user(payer)
                        .build()
        );


        /*
         * ---------------------------------------------------------
         * CATEGORY
         * ---------------------------------------------------------
         */
        category = categoryRepository.save(
                Category.builder()
                        .name("SettlementCategory-" + suffix)
                        .user(payer)
                        .build()
        );


        /*
         * ---------------------------------------------------------
         * EXPENSE
         * ---------------------------------------------------------
         */
        expense = expenseRepository.save(
                Expense.builder()
                        .title("Settlement Test Expense")
                        .description("Database invariant test")
                        .amount(new BigDecimal("100.00"))
                        .expenseAt(OffsetDateTime.now())
                        .status(ExpenseStatus.ACTIVE)
                        .user(payer)
                        .wallet(wallet)
                        .category(category)
                        .build()
        );


        /*
         * ---------------------------------------------------------
         * GROUP
         * ---------------------------------------------------------
         */
        group = groupRepository.save(
                Group.builder()
                        .name("SettlementGroup-" + suffix)
                        .user(payer)
                        .status(GroupStatus.ACTIVE)
                        .build()
        );


        /*
         * ---------------------------------------------------------
         * EXPENSE SHARE
         * ---------------------------------------------------------
         */
        expenseShare = expenseShareRepository.save(
                ExpenseShare.builder()
                        .expense(expense)
                        .user(payer)
                        .group(group)
                        .originalAmount(new BigDecimal("100.00"))
                        .remainingAmount(new BigDecimal("100.00"))
                        .status(ExpenseShareStatus.PENDING)
                        .build()
        );
    }


    private Settlement buildSettlement(
            BigDecimal amount,
            User settlementPayer,
            User settlementReceiver,
            SettlementStatus status,
            Wallet settlementWallet
    ) {

        return Settlement.builder()
                .amount(amount)
                .payer(settlementPayer)
                .receiver(settlementReceiver)
                .expenseShare(expenseShare)
                .category(category)
                .wallet(settlementWallet)
                .settledAt(OffsetDateTime.now())
                .status(status)
                .build();
    }


    @Test
    void shouldRejectZeroAmount() {

        Settlement settlement = buildSettlement(
                BigDecimal.ZERO,
                payer,
                receiver,
                SettlementStatus.SETTLED,
                wallet
        );

        assertThrows(
                Exception.class,
                () -> settlementRepository.saveAndFlush(settlement)
        );
    }


    @Test
    void shouldRejectNegativeAmount() {

        Settlement settlement = buildSettlement(
                new BigDecimal("-10.00"),
                payer,
                receiver,
                SettlementStatus.SETTLED,
                wallet
        );

        assertThrows(
                Exception.class,
                () -> settlementRepository.saveAndFlush(settlement)
        );
    }


    @Test
    void shouldRejectNullAmount() {

        Settlement settlement = buildSettlement(
                null,
                payer,
                receiver,
                SettlementStatus.SETTLED,
                wallet
        );

        assertThrows(
                Exception.class,
                () -> settlementRepository.saveAndFlush(settlement)
        );
    }


    @Test
    void shouldRejectSamePayerAndReceiver() {

        Settlement settlement = buildSettlement(
                new BigDecimal("50.00"),
                payer,
                payer,
                SettlementStatus.SETTLED,
                wallet
        );

        assertThrows(
                Exception.class,
                () -> settlementRepository.saveAndFlush(settlement)
        );
    }


    @Test
    void shouldRejectNullStatus() {

        Settlement settlement = buildSettlement(
                new BigDecimal("50.00"),
                payer,
                receiver,
                null,
                wallet
        );

        assertThrows(
                Exception.class,
                () -> settlementRepository.saveAndFlush(settlement)
        );
    }


    @Test
    void shouldRejectNullWallet() {

        Settlement settlement = buildSettlement(
                new BigDecimal("50.00"),
                payer,
                receiver,
                SettlementStatus.SETTLED,
                null
        );

        assertThrows(
                Exception.class,
                () -> settlementRepository.saveAndFlush(settlement)
        );
    }
}