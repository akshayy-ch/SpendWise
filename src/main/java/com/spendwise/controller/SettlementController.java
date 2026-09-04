package com.spendwise.controller;

import com.spendwise.dto.request.settlement.CreateSettlementRequest;
import com.spendwise.dto.response.settlement.SettlementResponse;
import com.spendwise.service.SettlementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    @PostMapping("/expense-shares/{expenseShareId}")
    public ResponseEntity<SettlementResponse> createSettlement(@PathVariable UUID expenseShareId, @Valid @RequestBody CreateSettlementRequest request) {
        SettlementResponse response = settlementService.createSettlement(expenseShareId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{settlementId}")
    public ResponseEntity<SettlementResponse> getSettlement(@PathVariable UUID settlementId) {
        SettlementResponse response = settlementService.getSettlement(settlementId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<SettlementResponse>> getMySettlements() {
        List<SettlementResponse> response = settlementService.getMySettlements();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/expense-shares/{expenseShareId}")
    public ResponseEntity<List<SettlementResponse>> getSettlementsForShare(
            @PathVariable UUID expenseShareId) {

        List<SettlementResponse> response = settlementService.getSettlementsForShare(expenseShareId);

        return ResponseEntity.ok(response);
    }
}