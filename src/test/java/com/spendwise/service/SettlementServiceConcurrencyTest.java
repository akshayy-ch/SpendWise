package com.spendwise.service;

import com.spendwise.dto.request.settlement.CreateSettlementRequest;
import com.spendwise.entity.Category;
import com.spendwise.entity.Expense;
import com.spendwise.entity.ExpenseShare;
import com.spendwise.entity.Group;
import com.spendwise.entity.GroupMember;
import com.spendwise.entity.GroupMemberId;
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
import com.spendwise.repository.SettlementRepository;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class SettlementServiceConcurrencyTest {

    @Autowired
    private SettlementService settlementService;

    @Autowired
    private SettlementRepository settlementRepository;

    @Autowired
    private ExpenseShareRepository expenseShareRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;


    private User debtor;
    private User payer;

    private Wallet wallet;
    private Wallet payerWallet;

    private Category category;

    private Expense expense;
    private Expense secondExpense;

    private ExpenseShare expenseShare;
    private ExpenseShare secondExpenseShare;

    private Group group;


    @BeforeEach
    void setUp() {

        String uniqueId = UUID.randomUUID().toString();
        String shortId = uniqueId.substring(0, 8);


        /*
         * ---------------------------------------------------------
         * DEBTOR
         * ---------------------------------------------------------
         */

        debtor = User.builder()
                .username("settlement_debtor_" + uniqueId)
                .name("Settlement Debtor")
                .email("settlement_debtor_" + uniqueId + "@test.com")
                .build();

        debtor.updatePasswordHash("test-password");

        debtor = userRepository.save(debtor);


        /*
         * ---------------------------------------------------------
         * PAYER / ORIGINAL EXPENSE OWNER
         * ---------------------------------------------------------
         */

        payer = User.builder()
                .username("settlement_payer_" + uniqueId)
                .name("Settlement Payer")
                .email("settlement_payer_" + uniqueId + "@test.com")
                .build();

        payer.updatePasswordHash("test-password");

        payer = userRepository.save(payer);


        /*
         * ---------------------------------------------------------
         * PAYER WALLET
         * ---------------------------------------------------------
         */

        payerWallet = Wallet.builder()
                .walletName("PayerWallet-" + shortId)
                .type(WalletType.CASH)
                .currentBalance(new BigDecimal("500.00"))
                .status(WalletStatus.ACTIVE)
                .user(payer)
                .build();

        payerWallet = walletRepository.save(payerWallet);


        /*
         * ---------------------------------------------------------
         * GROUP
         * ---------------------------------------------------------
         */

        group = Group.builder()
                .name("SettlementTest-" + shortId)
                .description("Concurrency test")
                .status(GroupStatus.ACTIVE)
                .user(payer)
                .build();

        group = groupRepository.save(group);


        /*
         * ---------------------------------------------------------
         * GROUP MEMBER
         * ---------------------------------------------------------
         */

        GroupMemberId groupMemberId =
                new GroupMemberId(
                        group.getId(),
                        debtor.getId()
                );

        GroupMember groupMember = GroupMember.builder()
                .id(groupMemberId)
                .group(group)
                .user(debtor)
                .status(GroupMemberStatus.ACTIVE)
                .build();

        groupMemberRepository.save(groupMember);


        /*
         * ---------------------------------------------------------
         * DEBTOR WALLET
         *
         * This is the wallet that will actually be used
         * for the settlements.
         * ---------------------------------------------------------
         */

        wallet = Wallet.builder()
                .walletName("SettlementWallet-" + shortId)
                .type(WalletType.CASH)
                .currentBalance(new BigDecimal("200.00"))
                .status(WalletStatus.ACTIVE)
                .user(debtor)
                .build();

        wallet = walletRepository.save(wallet);


        /*
         * ---------------------------------------------------------
         * CATEGORY
         * ---------------------------------------------------------
         */

        category = categoryRepository
                .findByNameAndIsSystemTrue("Food")
                .orElseThrow();


        /*
         * ---------------------------------------------------------
         * FIRST EXPENSE
         * ---------------------------------------------------------
         */

        expense = Expense.builder()
                .status(ExpenseStatus.ACTIVE)
                .title("Settlement Concurrency Expense")
                .amount(new BigDecimal("100.00"))
                .expenseAt(OffsetDateTime.now())
                .user(payer)
                .wallet(payerWallet)
                .category(category)
                .build();

        expense = expenseRepository.save(expense);


        /*
         * ---------------------------------------------------------
         * FIRST EXPENSE SHARE
         * ---------------------------------------------------------
         *
         * Debtor owes payer 100.
         * ---------------------------------------------------------
         */

        expenseShare = ExpenseShare.builder()
                .expense(expense)
                .user(debtor)
                .group(group)
                .originalAmount(new BigDecimal("100.00"))
                .remainingAmount(new BigDecimal("100.00"))
                .status(ExpenseShareStatus.PENDING)
                .build();

        expenseShare = expenseShareRepository.save(expenseShare);


        /*
         * ---------------------------------------------------------
         * SECOND EXPENSE
         * ---------------------------------------------------------
         */

        secondExpense = Expense.builder()
                .status(ExpenseStatus.ACTIVE)
                .title("Second Settlement Concurrency Expense")
                .amount(new BigDecimal("100.00"))
                .expenseAt(OffsetDateTime.now())
                .user(payer)
                .wallet(payerWallet)
                .category(category)
                .build();

        secondExpense = expenseRepository.save(secondExpense);


        /*
         * ---------------------------------------------------------
         * SECOND EXPENSE SHARE
         * ---------------------------------------------------------
         */

        secondExpenseShare = ExpenseShare.builder()
                .expense(secondExpense)
                .user(debtor)
                .group(group)
                .originalAmount(new BigDecimal("100.00"))
                .remainingAmount(new BigDecimal("100.00"))
                .status(ExpenseShareStatus.PENDING)
                .build();

        secondExpenseShare =
                expenseShareRepository.save(secondExpenseShare);


        /*
         * ---------------------------------------------------------
         * DEFAULT TEST THREAD AUTHENTICATION
         * ---------------------------------------------------------
         */

        setAuthentication();
    }


    private void setAuthentication() {

        SpendWiseUserDetails userDetails =
                new SpendWiseUserDetails(debtor);

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


    /*
     * =========================================================
     * EXISTING CONCURRENCY TEST
     * =========================================================
     *
     * Two transactions attempt to settle the SAME
     * ExpenseShare simultaneously.
     *
     * Expected:
     *
     * 1 succeeds
     * 1 fails
     *
     * This verifies the ExpenseShare pessimistic lock.
     * =========================================================
     */

    @Test
    void concurrentSettlement_shouldSettleOnlyOnce()
            throws Exception {

        CreateSettlementRequest firstRequest =
                CreateSettlementRequest.builder()
                        .receiverId(payer.getId())
                        .amount(new BigDecimal("70.00"))
                        .walletName(wallet.getWalletName())
                        .categoryName(category.getName())
                        .build();


        CreateSettlementRequest secondRequest =
                CreateSettlementRequest.builder()
                        .receiverId(payer.getId())
                        .amount(new BigDecimal("70.00"))
                        .walletName(wallet.getWalletName())
                        .categoryName(category.getName())
                        .build();


        ExecutorService executor =
                Executors.newFixedThreadPool(2);


        CountDownLatch startLatch =
                new CountDownLatch(1);


        Future<?> first =
                executor.submit(() -> {

                    try {

                        startLatch.await();

                        setAuthentication();

                        return settlementService.createSettlement(
                                expenseShare.getId(),
                                firstRequest
                        );

                    } catch (Exception e) {

                        throw new RuntimeException(e);
                    }
                });


        Future<?> second =
                executor.submit(() -> {

                    try {

                        startLatch.await();

                        setAuthentication();

                        return settlementService.createSettlement(
                                expenseShare.getId(),
                                secondRequest
                        );

                    } catch (Exception e) {

                        throw new RuntimeException(e);
                    }
                });


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


        assertEquals(1, successCount);

        assertEquals(1, failureCount);


        ExpenseShare finalShare =
                expenseShareRepository
                        .findById(expenseShare.getId())
                        .orElseThrow();


        assertEquals(
                new BigDecimal("30.00"),
                finalShare.getRemainingAmount()
        );


        assertEquals(
                ExpenseShareStatus.PENDING,
                finalShare.getStatus()
        );


        Wallet finalWallet =
                walletRepository
                        .findById(wallet.getId())
                        .orElseThrow();


        assertEquals(
                new BigDecimal("130.00"),
                finalWallet.getCurrentBalance()
        );


        long settlementCount =
                settlementRepository
                        .findAllByExpenseShareId(
                                expenseShare.getId()
                        )
                        .size();


        assertEquals(1, settlementCount);
    }


    /*
     * =========================================================
     * DEADLOCK / LOCK ORDER TEST
     * =========================================================
     *
     * Two transactions operate on DIFFERENT ExpenseShares
     * but use the SAME wallet.
     *
     * Expected lock order:
     *
     * Transaction 1:
     *
     *      ExpenseShare A
     *             ↓
     *          Wallet
     *
     *
     * Transaction 2:
     *
     *      ExpenseShare B
     *             ↓
     *          Wallet
     *
     *
     * Both transactions therefore acquire locks in the
     * same direction.
     *
     * The important assertion is that BOTH transactions
     * finish within the timeout.
     *
     * If there is a circular lock dependency, one or both
     * futures could remain blocked and the test would fail
     * with a timeout.
     * =========================================================
     */

    @Test
    void concurrentSettlementsOnDifferentShares_shouldNotDeadlock()
            throws Exception {

        CreateSettlementRequest firstRequest =
                CreateSettlementRequest.builder()
                        .receiverId(payer.getId())
                        .amount(new BigDecimal("50.00"))
                        .walletName(wallet.getWalletName())
                        .categoryName(category.getName())
                        .build();


        CreateSettlementRequest secondRequest =
                CreateSettlementRequest.builder()
                        .receiverId(payer.getId())
                        .amount(new BigDecimal("50.00"))
                        .walletName(wallet.getWalletName())
                        .categoryName(category.getName())
                        .build();


        ExecutorService executor =
                Executors.newFixedThreadPool(2);


        CountDownLatch startLatch =
                new CountDownLatch(1);


        Future<?> first =
                executor.submit(() -> {

                    try {

                        startLatch.await();

                        setAuthentication();

                        return settlementService.createSettlement(
                                expenseShare.getId(),
                                firstRequest
                        );

                    } catch (Exception e) {

                        throw new RuntimeException(e);
                    }
                });


        Future<?> second =
                executor.submit(() -> {

                    try {

                        startLatch.await();

                        setAuthentication();

                        return settlementService.createSettlement(
                                secondExpenseShare.getId(),
                                secondRequest
                        );

                    } catch (Exception e) {

                        throw new RuntimeException(e);
                    }
                });


        /*
         * Release both threads at approximately the same time.
         */

        startLatch.countDown();


        /*
         * Each transaction must complete within 10 seconds.
         *
         * A timeout here indicates that the transactions
         * may be stuck waiting on locks.
         */

        first.get(10, TimeUnit.SECONDS);

        second.get(10, TimeUnit.SECONDS);


        executor.shutdown();


        /*
         * ---------------------------------------------------------
         * VERIFY FIRST EXPENSE SHARE
         * ---------------------------------------------------------
         */

        ExpenseShare finalFirstShare =
                expenseShareRepository
                        .findById(expenseShare.getId())
                        .orElseThrow();


        assertEquals(
                new BigDecimal("50.00"),
                finalFirstShare.getRemainingAmount()
        );


        /*
         * ---------------------------------------------------------
         * VERIFY SECOND EXPENSE SHARE
         * ---------------------------------------------------------
         */

        ExpenseShare finalSecondShare =
                expenseShareRepository
                        .findById(secondExpenseShare.getId())
                        .orElseThrow();


        assertEquals(
                new BigDecimal("50.00"),
                finalSecondShare.getRemainingAmount()
        );


        /*
         * ---------------------------------------------------------
         * VERIFY WALLET
         * ---------------------------------------------------------
         *
         * Initial balance = 200
         *
         * Settlement 1 = -50
         * Settlement 2 = -50
         *
         * Final balance = 100
         * ---------------------------------------------------------
         */

        Wallet finalWallet =
                walletRepository
                        .findById(wallet.getId())
                        .orElseThrow();


        assertEquals(
                new BigDecimal("100.00"),
                finalWallet.getCurrentBalance()
        );


        /*
         * ---------------------------------------------------------
         * VERIFY FIRST SETTLEMENT
         * ---------------------------------------------------------
         */

        long firstSettlementCount =
                settlementRepository
                        .findAllByExpenseShareId(
                                expenseShare.getId()
                        )
                        .size();


        assertEquals(1, firstSettlementCount);


        /*
         * ---------------------------------------------------------
         * VERIFY SECOND SETTLEMENT
         * ---------------------------------------------------------
         */

        long secondSettlementCount =
                settlementRepository
                        .findAllByExpenseShareId(
                                secondExpenseShare.getId()
                        )
                        .size();


        assertEquals(1, secondSettlementCount);
    }
}