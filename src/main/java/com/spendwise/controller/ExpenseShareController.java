package com.spendwise.controller;

import com.spendwise.dto.error.ErrorResponse;
import com.spendwise.dto.request.expenseShare.CreateExpenseShareRequest;
import com.spendwise.dto.response.expenseShare.ExpenseShareResponse;
import com.spendwise.service.ExpenseShareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/expense-shares")
@RequiredArgsConstructor
@Tag(
        name = "Expense Shares",
        description = "APIs for creating and retrieving expense shares within groups"
)
public class ExpenseShareController {

    private final ExpenseShareService expenseShareService;


    @Operation(
            summary = "Create expense shares",
            description = "Creates expense shares for members of a group."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Expense shares created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid expense share data",
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
                    responseCode = "403",
                    description = "User is not authorized to create shares",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Expense or group not found",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Expense shares already exist or request conflicts with existing data",
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

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @Operation(
            summary = "Get expense shares",
            description = "Retrieves all shares associated with a specific expense."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Expense shares retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User is not authorized to access these shares",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Expense shares not found",
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
    @GetMapping("/expense/{expenseId}")
    public ResponseEntity<List<ExpenseShareResponse>> getExpenseSharesByExpense(
            @PathVariable UUID expenseId) {

        List<ExpenseShareResponse> response =
                expenseShareService.getExpenseSharesByExpense(expenseId);

        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "Get my expense shares",
            description = "Retrieves expense shares associated with the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Expense shares retrieved successfully"
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
    @GetMapping("/my")
    public ResponseEntity<List<ExpenseShareResponse>> getMyExpenseShares() {

        List<ExpenseShareResponse> response =
                expenseShareService.getMyExpenseShares();

        return ResponseEntity.ok(response);
    }
}