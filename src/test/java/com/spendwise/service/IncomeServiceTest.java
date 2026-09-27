package com.spendwise.service;

import com.spendwise.dto.request.income.CreateIncomeRequest;
import com.spendwise.entity.Income;
import com.spendwise.entity.User;
import com.spendwise.entity.Wallet;
import com.spendwise.enums.WalletStatus;
import com.spendwise.exception.WalletExceptions.ArchivedWalletException;
import com.spendwise.exception.WalletExceptions.InvalidAmountException;
import com.spendwise.exception.WalletExceptions.WalletDoesNotExist;
import com.spendwise.repository.IncomeRepository;
import com.spendwise.repository.UserRepository;
import com.spendwise.repository.WalletRepository;
import com.spendwise.security.SpendWiseUserDetails;
import com.spendwise.service.IncomeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class IncomeServiceTest {

    @Autowired
    private IncomeService incomeService;

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private Wallet wallet;

    @BeforeEach
    void setUp() {

        String id = UUID.randomUUID().toString();

        user = User.builder()
                .username("income_service_" + id)
                .name("Income Service Test")
                .email("income_service_" + id + "@test.com")
                .build();

        user.updatePasswordHash("test-password");

        user = userRepository.save(user);

        wallet = Wallet.builder()
                .walletName("Income Test Wallet")
                .type(com.spendwise.enums.WalletType.CASH)
                .currentBalance(new BigDecimal("500.00"))
                .status(WalletStatus.ACTIVE)
                .user(user)
                .build();

        wallet = walletRepository.save(wallet);

        setAuthentication();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setAuthentication() {

        SpendWiseUserDetails userDetails =
                new SpendWiseUserDetails(user);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                )
        );
    }

    private CreateIncomeRequest request(BigDecimal amount) {

        return CreateIncomeRequest.builder()
                .source("Salary")
                .walletName("Income Test Wallet")
                .description("Monthly salary")
                .amount(amount)
                .incomeAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void createIncome_shouldIncreaseWalletBalanceAndPersistIncome() {

        incomeService.createIncome(
                request(new BigDecimal("250.00"))
        );

        Wallet updatedWallet =
                walletRepository.findAllByUserId(user.getId())
                        .stream()
                        .filter(w ->
                                w.getWalletName()
                                        .equals("Income Test Wallet")
                        )
                        .findFirst()
                        .orElseThrow();

        assertEquals(
                new BigDecimal("750.00"),
                updatedWallet.getCurrentBalance()
        );

        Income income =
                incomeRepository.findByUserId(user.getId())
                        .stream()
                        .filter(i ->
                                i.getSource().equals("Salary")
                        )
                        .findFirst()
                        .orElseThrow();

        assertEquals(
                new BigDecimal("250.00"),
                income.getAmount()
        );

        assertEquals(
                "Salary",
                income.getSource()
        );

        assertEquals(
                user.getId(),
                income.getUser().getId()
        );

        assertEquals(
                wallet.getId(),
                income.getWallet().getId()
        );
    }

    @Test
    void createIncome_shouldRejectMissingWallet() {

        CreateIncomeRequest request =
                CreateIncomeRequest.builder()
                        .source("Salary")
                        .walletName("Non Existing Wallet")
                        .description("Monthly salary")
                        .amount(new BigDecimal("250.00"))
                        .incomeAt(OffsetDateTime.now())
                        .build();

        assertThrows(
                WalletDoesNotExist.class,
                () -> incomeService.createIncome(request)
        );
    }

    @Test
    void createIncome_shouldRejectArchivedWallet() {

        wallet.setStatus(WalletStatus.ARCHIVED);
        walletRepository.save(wallet);

        assertThrows(
                ArchivedWalletException.class,
                () -> incomeService.createIncome(
                        request(new BigDecimal("250.00"))
                )
        );

        Wallet updatedWallet =
                walletRepository.findAllByUserId(user.getId())
                        .stream()
                        .filter(w ->
                                w.getWalletName()
                                        .equals("Income Test Wallet")
                        )
                        .findFirst()
                        .orElseThrow();

        assertEquals(
                new BigDecimal("500.00"),
                updatedWallet.getCurrentBalance()
        );
    }

    @Test
    void createIncome_shouldRejectZeroAmount() {

        assertThrows(
                InvalidAmountException.class,
                () -> incomeService.createIncome(
                        request(BigDecimal.ZERO)
                )
        );

        Wallet updatedWallet =
                walletRepository.findAllByUserId(user.getId())
                        .stream()
                        .filter(w ->
                                w.getWalletName()
                                        .equals("Income Test Wallet")
                        )
                        .findFirst()
                        .orElseThrow();

        assertEquals(
                new BigDecimal("500.00"),
                updatedWallet.getCurrentBalance()
        );
    }

    @Test
    void createIncome_shouldRejectNegativeAmount() {

        assertThrows(
                InvalidAmountException.class,
                () -> incomeService.createIncome(
                        request(new BigDecimal("-100.00"))
                )
        );

        Wallet updatedWallet =
                walletRepository.findAllByUserId(user.getId())
                        .stream()
                        .filter(w ->
                                w.getWalletName()
                                        .equals("Income Test Wallet")
                        )
                        .findFirst()
                        .orElseThrow();

        assertEquals(
                new BigDecimal("500.00"),
                updatedWallet.getCurrentBalance()
        );
    }

    @Test
    void createIncome_shouldRejectAnotherUsersWallet() {

        String id = UUID.randomUUID().toString();

        User secondUser =
                User.builder()
                        .username("income_other_" + id)
                        .name("Other Income User")
                        .email("income_other_" + id + "@test.com")
                        .build();

        secondUser.updatePasswordHash("test-password");

        secondUser = userRepository.save(secondUser);

        Wallet secondWallet =
                Wallet.builder()
                        .walletName("Other User Wallet")
                        .type(com.spendwise.enums.WalletType.CASH)
                        .currentBalance(new BigDecimal("300.00"))
                        .status(WalletStatus.ACTIVE)
                        .user(secondUser)
                        .build();

        walletRepository.save(secondWallet);

        assertThrows(
                WalletDoesNotExist.class,
                () -> incomeService.createIncome(
                        CreateIncomeRequest.builder()
                                .source("Salary")
                                .walletName("Other User Wallet")
                                .description("Unauthorized income")
                                .amount(new BigDecimal("100.00"))
                                .incomeAt(OffsetDateTime.now())
                                .build()
                )
        );
    }

    @Test
    void createIncome_shouldAllowMultipleIncomeEntries() {

        incomeService.createIncome(
                request(new BigDecimal("100.00"))
        );

        incomeService.createIncome(
                request(new BigDecimal("200.00"))
        );

        Wallet updatedWallet =
                walletRepository.findAllByUserId(user.getId())
                        .stream()
                        .filter(w ->
                                w.getWalletName()
                                        .equals("Income Test Wallet")
                        )
                        .findFirst()
                        .orElseThrow();

        assertEquals(
                new BigDecimal("800.00"),
                updatedWallet.getCurrentBalance()
        );

        long incomeCount =
                incomeRepository.findByUserId(user.getId())
                        .stream()
                        .filter(i ->
                                i.getSource().equals("Salary")
                        )
                        .count();

        assertEquals(2, incomeCount);
    }
}