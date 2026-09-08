package com.spendwise.controller;

import com.spendwise.dto.request.budget.UpdateBudgetRequest;
import com.spendwise.dto.request.budget.createBudgetRequest;
import com.spendwise.dto.response.budget.BudgetResponse;
import com.spendwise.service.BudgetServices;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetServices budgetServices;

    @PostMapping
    public BudgetResponse createBudget(@Valid @RequestBody createBudgetRequest request) {
        System.out.println("Reached 1");
        return budgetServices.createBudget(request);
    }

    @GetMapping("/current")
    public BudgetResponse getCurrentBudget() {
        return budgetServices.getCurrentBudget();
    }

    @PatchMapping("/current")
    public BudgetResponse updateBudget(@Valid @RequestBody UpdateBudgetRequest request) {

        return budgetServices.updateBudget(request);
    }

    @DeleteMapping("/current")
    public ResponseEntity<Void> deleteBudget() {
        budgetServices.deleteBudget();
        return ResponseEntity.noContent().build();
    }
}