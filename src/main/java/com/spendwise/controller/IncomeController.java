package com.spendwise.controller;

import com.spendwise.dto.error.ErrorResponse;
import com.spendwise.dto.request.income.CreateIncomeRequest;
import com.spendwise.dto.request.income.GetIncomeRequest;
import com.spendwise.dto.response.income.IncomePageResponse;
import com.spendwise.dto.response.income.IncomeResponse;
import com.spendwise.service.IncomeService;
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

@RestController
@RequestMapping("/api/incomes")
@RequiredArgsConstructor
@Tag(
        name = "Income",
        description = "APIs for creating and retrieving user income records"
)
public class IncomeController {

    private final IncomeService incomeService;


    @Operation(
            summary = "Create income",
            description = "Creates an income record for the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Income created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid income data",
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
    @PostMapping("/createIncome")
    public ResponseEntity<IncomeResponse> createIncome(
            @Valid @RequestBody CreateIncomeRequest request) {

        IncomeResponse response = incomeService.createIncome(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @Operation(
            summary = "Get income records",
            description = "Retrieves the authenticated user's income records with pagination, sorting, filtering, and search."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Income records retrieved successfully"
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
    @GetMapping("/getIncome")
    public ResponseEntity<IncomePageResponse> getIncomes(
            @Valid GetIncomeRequest request,
            @Parameter(description = "Pagination and sorting parameters")
            Pageable pageable) {

        return ResponseEntity.ok(
                incomeService.getIncomes(request, pageable)
        );
    }
}