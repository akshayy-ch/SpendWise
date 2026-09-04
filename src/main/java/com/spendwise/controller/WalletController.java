package com.spendwise.controller;

import com.spendwise.dto.request.wallet.ActivateRequest;
import com.spendwise.dto.request.wallet.ArchiveRequest;
import com.spendwise.dto.request.wallet.CreateWalletRequest;
import com.spendwise.dto.request.wallet.GetWalletsRequest;
import com.spendwise.dto.response.wallet.WalletResponse;
import com.spendwise.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    @PostMapping("/createWallet")
    public ResponseEntity<WalletResponse> createWallet(@Valid @RequestBody CreateWalletRequest request){
        WalletResponse response = walletService.createWallet(request);
        return ResponseEntity.status(HttpStatus.CREATED). body(response);
    }

    @GetMapping("/getWallet")
    public ResponseEntity<WalletResponse> getWallet(@Valid GetWalletsRequest request){
        WalletResponse response = walletService.getWallet(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/getWallets")
    public ResponseEntity<List<WalletResponse>> getWallets() {
        return ResponseEntity.ok(walletService.getWallets());
    }

    @PatchMapping("/archiveWallet")
    public ResponseEntity<WalletResponse> archiveWallet(@Valid @RequestBody  ArchiveRequest request){
        WalletResponse response = walletService.archiveWallet(request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/activateWallet")
    public ResponseEntity<WalletResponse> activateWallet(@Valid @RequestBody ActivateRequest request){
        WalletResponse response = walletService.activateWallet(request);
        return ResponseEntity.ok(response);
    }

}
