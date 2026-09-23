package com.spendwise.service;

import com.spendwise.dto.request.settlement.CreateSettlementRequest;
import com.spendwise.entity.*;
import com.spendwise.enums.ExpenseShareStatus;
import com.spendwise.enums.ExpenseStatus;
import com.spendwise.enums.GroupMemberStatus;
import com.spendwise.enums.GroupStatus;
import com.spendwise.enums.WalletStatus;
import com.spendwise.enums.WalletType;
import com.spendwise.mapper.SettlementMapper;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class SettlementServiceRollbackTest {

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

    @MockBean
    private SettlementMapper settlementMapper;

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
                .username("rollback_debtor_" + uniqueId)
                .name("Rollback Debtor")
                .email("rollback_debtor_" + uniqueId + "@test.com")
                .build();

        debtor.updatePasswordHash("test-password");
        debtor = userRepository.save(debtor);

        payer = User.builder()
                .username("rollback_payer_" + uniqueId)
                .name("Rollback Payer")
                .email("rollback_payer_" + uniqueId + "@test.com")
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
                .name("RollbackTest-" + shortId)
                .description("Rollback test")
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
                .walletName("RollbackWallet-" + shortId)
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
                .title("Settlement Rollback Expense")
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
    void settlementFailure_shouldRollbackWalletExpenseShareAndSettlement() throws Exception {

        assertEquals(
                new BigDecimal("200.00"),
                wallet.getCurrentBalance()
        );

        assertEquals(
                new BigDecimal("100.00"),
                expenseShare.getRemainingAmount()
        );

        assertEquals(
                0,
                settlementRepository
                        .findAllByExpenseShareId(expenseShare.getId())
                        .size()
        );

        CreateSettlementRequest request =
                CreateSettlementRequest.builder()
                        .receiverId(payer.getId())
                        .amount(new BigDecimal("70.00"))
                        .walletName(wallet.getWalletName())
                        .categoryName(category.getName())
                        .build();

        when(settlementMapper.toResponse(any(Settlement.class)))
                .thenThrow(
                        new RuntimeException(
                                "Forced failure for rollback test"
                        )
                );

        assertThrows(
                RuntimeException.class,
                () -> settlementService.createSettlement(
                        expenseShare.getId(),
                        request
                )
        );

        Wallet finalWallet =
                walletRepository
                        .findById(wallet.getId())
                        .orElseThrow();

        assertEquals(
                new BigDecimal("200.00"),
                finalWallet.getCurrentBalance()
        );

        ExpenseShare finalShare =
                expenseShareRepository
                        .findById(expenseShare.getId())
                        .orElseThrow();

        assertEquals(
                new BigDecimal("100.00"),
                finalShare.getRemainingAmount()
        );

        assertEquals(
                ExpenseShareStatus.PENDING,
                finalShare.getStatus()
        );

        long settlementCount =
                settlementRepository
                        .findAllByExpenseShareId(
                                expenseShare.getId()
                        )
                        .size();

        assertEquals(0, settlementCount);
    }
}