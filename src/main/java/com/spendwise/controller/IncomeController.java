package com.spendwise.controller;

import com.spendwise.dto.request.income.CreateIncomeRequest;
import com.spendwise.dto.request.income.GetIncomeRequest;
import com.spendwise.dto.response.income.IncomePageResponse;
import com.spendwise.dto.response.income.IncomeResponse;
import com.spendwise.service.IncomeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/incomes")
@RequiredArgsConstructor
public class IncomeController {
    private final IncomeService incomeService;

    @PostMapping("/createIncome")
    public ResponseEntity<IncomeResponse> createIncome(@Valid @RequestBody CreateIncomeRequest request){
        IncomeResponse response = incomeService.createIncome(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/getIncome")
    public ResponseEntity<IncomePageResponse> getIncomes(@Valid GetIncomeRequest request, Pageable pageable){
        return ResponseEntity.ok(incomeService.getIncomes(request, pageable));
    }
}