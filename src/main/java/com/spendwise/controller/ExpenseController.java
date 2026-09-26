package com.spendwise.controller;

import com.spendwise.dto.error.ErrorResponse;
import com.spendwise.dto.request.expense.CreateExpenseRequest;
import com.spendwise.dto.request.expense.GetExpenseRequest;
import com.spendwise.dto.response.expense.ExpensePageResponse;
import com.spendwise.dto.response.expense.ExpenseResponse;
import com.spendwise.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@Tag(
        name = "Expenses",
        description = "APIs for creating, retrieving, filtering, and voiding expenses"
)
public class ExpenseController {

    private final ExpenseService expenseService;


    @Operation(
            summary = "Create an expense",
            description = "Creates an expense for the authenticated user and deducts the expense amount from the selected wallet."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Expense created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid expense data",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Wallet or category not found",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Request conflicts with the current resource state",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping("/createExpense")
    public ResponseEntity<ExpenseResponse> createExpense(
            @Valid @RequestBody CreateExpenseRequest request) {

        ExpenseResponse response = expenseService.createExpense(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @Operation(
            summary = "Get expenses",
            description = "Retrieves the authenticated user's expenses with support for pagination, sorting, filtering, and search."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Expenses retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid filter, search, sorting, or pagination parameters",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @GetMapping("/getExpenses")
    public ResponseEntity<ExpensePageResponse> getExpenses(
            @Valid GetExpenseRequest request,
            @Parameter(description = "Pagination and sorting parameters")
            Pageable pageable) {

        return ResponseEntity.ok(
                expenseService.getExpenses(request, pageable)
        );
    }


    @Operation(
            summary = "Void an expense",
            description = "Voids an existing expense. Expenses are not physically deleted."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Expense voided successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Expense or wallet not found",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Expense cannot be voided in its current state",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PatchMapping("/{expenseId}/void")
    public ResponseEntity<ExpenseResponse> voidExpense(
            @PathVariable UUID expenseId) {

        ExpenseResponse response = expenseService.voidExpense(expenseId);

        return ResponseEntity.ok(response);
    }
}