package com.spendwise.controller;

import com.spendwise.dto.error.ErrorResponse;
import com.spendwise.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(
        name = "Analytics",
        description = "APIs for retrieving personal finance and expense-sharing analytics"
)
public class AnalyticsController {

    private final AnalyticsService analyticsService;


    @Operation(
            summary = "Get spending this month",
            description = "Returns the total amount spent by the authenticated user during the current month."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Monthly spending retrieved successfully"
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
    @GetMapping("/spent-this-month")
    public ResponseEntity<BigDecimal> getSpentThisMonth() {

        return ResponseEntity.ok(
                analyticsService.getSpentThisMonth()
        );
    }


    @Operation(
            summary = "Get remaining budget",
            description = "Returns the amount remaining from the authenticated user's current budget."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Remaining budget retrieved successfully"
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
                    description = "Current budget not found",
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
    @GetMapping("/budget-remaining")
    public ResponseEntity<BigDecimal> getBudgetRemaining() {

        return ResponseEntity.ok(
                analyticsService.getBudgetRemaining()
        );
    }


    @Operation(
            summary = "Get amount you owe",
            description = "Returns the total outstanding amount the authenticated user owes through expense sharing."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Outstanding amount retrieved successfully"
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
    @GetMapping("/you-owe")
    public ResponseEntity<BigDecimal> getYouOwe() {

        return ResponseEntity.ok(
                analyticsService.getYouOwe()
        );
    }


    @Operation(
            summary = "Get amount owed to you",
            description = "Returns the total outstanding amount other users owe to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Amount owed retrieved successfully"
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
    @GetMapping("/you-are-owed")
    public ResponseEntity<BigDecimal> getYouAreOwed() {

        return ResponseEntity.ok(
                analyticsService.getYouAreOwed()
        );
    }


    @Operation(
            summary = "Get spending by category",
            description = "Returns the user's spending grouped by category."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Category spending retrieved successfully"
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
    @GetMapping("/spending-by-category")
    public ResponseEntity<List<AnalyticsService.CategorySpendingResponse>> getSpendingByCategory() {

        return ResponseEntity.ok(
                analyticsService.getSpendingByCategory()
        );
    }


    @Operation(
            summary = "Get open groups",
            description = "Returns groups with outstanding expense shares for the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Open groups retrieved successfully"
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
    @GetMapping("/open-groups")
    public ResponseEntity<List<AnalyticsService.OpenGroupResponse>> getOpenGroups() {

        return ResponseEntity.ok(
                analyticsService.getOpenGroups()
        );
    }


    @Operation(
            summary = "Get recent activity",
            description = "Returns recent financial activity relevant to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Recent activity retrieved successfully"
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
    @GetMapping("/recent-activity")
    public ResponseEntity<List<AnalyticsService.RecentActivityResponse>> getRecentActivity() {

        return ResponseEntity.ok(
                analyticsService.getRecentActivity()
        );
    }
}