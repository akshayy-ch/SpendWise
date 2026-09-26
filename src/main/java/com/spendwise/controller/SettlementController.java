package com.spendwise.controller;

import com.spendwise.dto.error.ErrorResponse;
import com.spendwise.dto.request.settlement.CreateSettlementRequest;
import com.spendwise.dto.response.settlement.SettlementPageResponse;
import com.spendwise.dto.response.settlement.SettlementResponse;
import com.spendwise.service.SettlementService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/settlements")
@RequiredArgsConstructor
@Tag(
        name = "Settlements",
        description = "APIs for creating and retrieving expense share settlements"
)
public class SettlementController {

    private final SettlementService settlementService;


    @Operation(
            summary = "Create a settlement",
            description = "Creates a settlement against an expense share."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Settlement created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid settlement data",
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
                    description = "User is not authorized to settle this expense share",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Expense share not found",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Settlement conflicts with the current expense share state",
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
    @PostMapping("/expense-shares/{expenseShareId}")
    public ResponseEntity<SettlementResponse> createSettlement(
            @PathVariable UUID expenseShareId,
            @Valid @RequestBody CreateSettlementRequest request) {

        SettlementResponse response =
                settlementService.createSettlement(expenseShareId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @Operation(
            summary = "Get settlement",
            description = "Retrieves a settlement by its ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Settlement retrieved successfully"
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
                    description = "Settlement not found",
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
    @GetMapping("/{settlementId}")
    public ResponseEntity<SettlementResponse> getSettlement(
            @PathVariable UUID settlementId) {

        SettlementResponse response =
                settlementService.getSettlement(settlementId);

        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "Get my settlements",
            description = "Retrieves settlements associated with the authenticated user with pagination and sorting."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Settlements retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid pagination or sorting parameters",
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
    @GetMapping("/my")
    public ResponseEntity<SettlementPageResponse> getMySettlements(
            @Parameter(description = "Pagination and sorting parameters")
            Pageable pageable) {

        return ResponseEntity.ok(
                settlementService.getMySettlements(pageable)
        );
    }


    @Operation(
            summary = "Get settlements for expense share",
            description = "Retrieves all settlements associated with a specific expense share."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Settlements retrieved successfully"
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
                    description = "User is not authorized to access these settlements",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Expense share not found",
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
    @GetMapping("/expense-shares/{expenseShareId}")
    public ResponseEntity<List<SettlementResponse>> getSettlementsForShare(
            @PathVariable UUID expenseShareId) {

        List<SettlementResponse> response =
                settlementService.getSettlementsForShare(expenseShareId);

        return ResponseEntity.ok(response);
    }
}