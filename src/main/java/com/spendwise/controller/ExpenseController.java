package com.spendwise.controller;

import com.spendwise.dto.request.expense.CreateExpenseRequest;
import com.spendwise.dto.request.expense.GetExpenseRequest;
import com.spendwise.dto.response.expense.ExpenseResponse;
import com.spendwise.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {
    private final ExpenseService expenseService;

    @PostMapping("/createExpense")
    public ResponseEntity<ExpenseResponse> createExpense(@Valid @RequestBody  CreateExpenseRequest request){
        ExpenseResponse response = expenseService.createExpense(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/getExpenses")
    public ResponseEntity<List<ExpenseResponse>> getExpenses(@Valid GetExpenseRequest request){
        return ResponseEntity.ok(expenseService.getExpenses(request));
    }
    @PatchMapping("/{expenseId}/void")
    public ResponseEntity<ExpenseResponse> voidExpense(
            @PathVariable UUID expenseId) {

        ExpenseResponse response = expenseService.voidExpense(expenseId);

        return ResponseEntity.ok(response);
    }
}
