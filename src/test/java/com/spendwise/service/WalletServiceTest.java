package com.spendwise.service;

import com.spendwise.dto.request.wallet.ActivateRequest;
import com.spendwise.dto.request.wallet.ArchiveRequest;
import com.spendwise.dto.request.wallet.CreateWalletRequest;
import com.spendwise.dto.request.wallet.GetWalletsRequest;
import com.spendwise.entity.User;
import com.spendwise.entity.Wallet;
import com.spendwise.enums.WalletStatus;
import com.spendwise.exception.WalletExceptions.DuplicateWalletException;
import com.spendwise.exception.WalletExceptions.InvalidWalletException;
import com.spendwise.repository.UserRepository;
import com.spendwise.repository.WalletRepository;
import com.spendwise.security.SpendWiseUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WalletServiceTest {

    @Autowired
    private WalletService walletService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    private User user;

    @BeforeEach
    void setUp() {

        String id = UUID.randomUUID().toString();

        user = User.builder()
                .username("wallet_service_" + id)
                .name("Wallet Service Test")
                .email("wallet_service_" + id + "@test.com")
                .build();

        user.updatePasswordHash("test-password");

        user = userRepository.save(user);

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

    @Test
    void createWallet_shouldCreateActiveWalletWithRequestedBalance() {

        CreateWalletRequest request =
                CreateWalletRequest.builder()
                        .walletName("Primary Wallet")
                        .type("cash")
                        .initialBalance(new BigDecimal("500.00"))
                        .build();

        walletService.createWallet(request);

        Wallet wallet = walletRepository.findAllByUserId(user.getId())
                .stream()
                .filter(w -> w.getWalletName().equals("Primary Wallet"))
                .findFirst()
                .orElseThrow();

        assertEquals(
                "Primary Wallet",
                wallet.getWalletName()
        );

        assertEquals(
                new BigDecimal("500.00"),
                wallet.getCurrentBalance()
        );

        assertEquals(
                WalletStatus.ACTIVE,
                wallet.getStatus()
        );

        assertEquals(
                user.getId(),
                wallet.getUser().getId()
        );
    }

    @Test
    void createWallet_shouldDefaultBalanceToZero() {

        CreateWalletRequest request =
                CreateWalletRequest.builder()
                        .walletName("Zero Balance Wallet")
                        .type("cash")
                        .build();

        walletService.createWallet(request);

        Wallet wallet = walletRepository.findAllByUserId(user.getId())
                .stream()
                .filter(w -> w.getWalletName().equals("Zero Balance Wallet"))
                .findFirst()
                .orElseThrow();

        assertEquals(
                0,
                wallet.getCurrentBalance().compareTo(BigDecimal.ZERO)
        );

        assertEquals(
                WalletStatus.ACTIVE,
                wallet.getStatus()
        );
    }

    @Test
    void createWallet_shouldRejectDuplicateNameForSameUser() {

        CreateWalletRequest first =
                CreateWalletRequest.builder()
                        .walletName("Duplicate Wallet")
                        .type("cash")
                        .initialBalance(BigDecimal.ZERO)
                        .build();

        walletService.createWallet(first);

        CreateWalletRequest duplicate =
                CreateWalletRequest.builder()
                        .walletName("Duplicate Wallet")
                        .type("bank")
                        .initialBalance(BigDecimal.ZERO)
                        .build();

        assertThrows(
                DuplicateWalletException.class,
                () -> walletService.createWallet(duplicate)
        );
    }

    @Test
    void createWallet_shouldAllowSameNameForDifferentUsers() {

        walletService.createWallet(
                CreateWalletRequest.builder()
                        .walletName("Shared Name")
                        .type("cash")
                        .build()
        );

        String id = UUID.randomUUID().toString();

        User secondUser =
                User.builder()
                        .username("wallet_other_" + id)
                        .name("Other User")
                        .email("wallet_other_" + id + "@test.com")
                        .build();

        secondUser.updatePasswordHash("test-password");

        secondUser = userRepository.save(secondUser);

        SpendWiseUserDetails details =
                new SpendWiseUserDetails(secondUser);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        details,
                        null,
                        details.getAuthorities()
                )
        );

        assertDoesNotThrow(
                () -> walletService.createWallet(
                        CreateWalletRequest.builder()
                                .walletName("Shared Name")
                                .type("cash")
                                .build()
                )
        );
    }

    @Test
    void createWallet_shouldRejectInvalidWalletType() {

        CreateWalletRequest request =
                CreateWalletRequest.builder()
                        .walletName("Invalid Type Wallet")
                        .type("not-a-wallet-type")
                        .build();

        assertThrows(
                InvalidWalletException.class,
                () -> walletService.createWallet(request)
        );
    }

    @Test
    void archiveWallet_shouldChangeStatusToArchived() {

        walletService.createWallet(
                CreateWalletRequest.builder()
                        .walletName("Archive Test")
                        .type("cash")
                        .initialBalance(
                                new BigDecimal("100.00")
                        )
                        .build()
        );

        var archived =
                walletService.archiveWallet(
                        ArchiveRequest.builder()
                                .walletName("Archive Test")
                                .build()
                );

        assertEquals(
                "ARCHIVED",
                archived.getStatus()
        );

        Wallet wallet = walletRepository.findAllByUserId(user.getId())
                .stream()
                .filter(w -> w.getWalletName().equals("Archive Test"))
                .findFirst()
                .orElseThrow();

        assertEquals(
                WalletStatus.ARCHIVED,
                wallet.getStatus()
        );
    }

    @Test
    void activateWallet_shouldChangeStatusToActive() {

        walletService.createWallet(
                CreateWalletRequest.builder()
                        .walletName("Activate Test")
                        .type("cash")
                        .build()
        );

        walletService.archiveWallet(
                ArchiveRequest.builder()
                        .walletName("Activate Test")
                        .build()
        );

        var activated =
                walletService.activateWallet(
                        ActivateRequest.builder()
                                .walletName("Activate Test")
                                .build()
                );

        assertEquals(
                "ACTIVE",
                activated.getStatus()
        );

        Wallet wallet = walletRepository.findAllByUserId(user.getId())
                .stream()
                .filter(w -> w.getWalletName().equals("Activate Test"))
                .findFirst()
                .orElseThrow();

        assertEquals(
                WalletStatus.ACTIVE,
                wallet.getStatus()
        );
    }

    @Test
    void getWallet_shouldReturnCurrentUsersWallet() {

        walletService.createWallet(
                CreateWalletRequest.builder()
                        .walletName("Owned Wallet")
                        .type("cash")
                        .build()
        );

        assertDoesNotThrow(
                () -> walletService.getWallet(
                        GetWalletsRequest.builder()
                                .walletName("Owned Wallet")
                                .build()
                )
        );
    }
}
