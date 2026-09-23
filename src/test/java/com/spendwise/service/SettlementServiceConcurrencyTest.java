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
    private ExpenseShare expenseShare;
    private Group group;

    @BeforeEach
    void setUp() {

        String uniqueId = UUID.randomUUID().toString();
        String shortId = uniqueId.substring(0, 8);

        debtor = User.builder()
                .username("settlement_debtor_" + uniqueId)
                .name("Settlement Debtor")
                .email("settlement_debtor_" + uniqueId + "@test.com")
                .build();

        debtor.updatePasswordHash("test-password");
        debtor = userRepository.save(debtor);

        payer = User.builder()
                .username("settlement_payer_" + uniqueId)
                .name("Settlement Payer")
                .email("settlement_payer_" + uniqueId + "@test.com")
                .build();

        payer.updatePasswordHash("test-password");
        payer = userRepository.save(payer);

        payerWallet = Wallet.builder()
                .walletName("PayerWallet-" + shortId)
                .type(WalletType.CASH)
                .currentBalance(new BigDecimal("500.00"))
                .status(WalletStatus.ACTIVE)
                .user(payer)
                .build();

        payerWallet = walletRepository.save(payerWallet);

        group = Group.builder()
                .name("SettlementTest-" + shortId)
                .description("Concurrency test")
                .status(GroupStatus.ACTIVE)
                .user(payer)
                .build();

        group = groupRepository.save(group);

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

        wallet = Wallet.builder()
                .walletName("SettlementWallet-" + shortId)
                .type(WalletType.CASH)
                .currentBalance(new BigDecimal("200.00"))
                .status(WalletStatus.ACTIVE)
                .user(debtor)
                .build();

        wallet = walletRepository.save(wallet);

        category = categoryRepository
                .findByNameAndIsSystemTrue("Food")
                .orElseThrow();

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

        expenseShare = ExpenseShare.builder()
                .expense(expense)
                .user(debtor)
                .group(group)
                .originalAmount(new BigDecimal("100.00"))
                .remainingAmount(new BigDecimal("100.00"))
                .status(ExpenseShareStatus.PENDING)
                .build();

        expenseShare = expenseShareRepository.save(expenseShare);

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

    @Test
    void concurrentSettlement_shouldSettleOnlyOnce() throws Exception {

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
}