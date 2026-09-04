package com.spendwise.controller;

import com.spendwise.dto.request.expenseShare.CreateExpenseShareRequest;
import com.spendwise.dto.response.expenseShare.ExpenseShareResponse;
import com.spendwise.service.ExpenseShareService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/expense-shares")
@RequiredArgsConstructor
public class ExpenseShareController {

    private final ExpenseShareService expenseShareService;

    @PostMapping("/expenses/{expenseId}/groups/{groupId}")
    public ResponseEntity<List<ExpenseShareResponse>> createShares(
            @PathVariable UUID expenseId,
            @PathVariable UUID groupId,
            @Valid @RequestBody CreateExpenseShareRequest request) {

        List<ExpenseShareResponse> response =
                expenseShareService.createShares(
                        expenseId,
                        groupId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/expense/{expenseId}")
    public ResponseEntity<List<ExpenseShareResponse>> getExpenseSharesByExpense(
            @PathVariable UUID expenseId) {

        List<ExpenseShareResponse> response =
                expenseShareService.getExpenseSharesByExpense(expenseId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<ExpenseShareResponse>> getMyExpenseShares() {

        List<ExpenseShareResponse> response =
                expenseShareService.getMyExpenseShares();

        return ResponseEntity.ok(response);
    }
}