package com.spendwise.controller;

import com.spendwise.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/spent-this-month")
    public BigDecimal getSpentThisMonth() {
        return analyticsService.getSpentThisMonth();
    }

    @GetMapping("/budget-remaining")
    public BigDecimal getBudgetRemaining() {
        return analyticsService.getBudgetRemaining();
    }

    @GetMapping("/you-owe")
    public BigDecimal getYouOwe() {
        return analyticsService.getYouOwe();
    }

    @GetMapping("/you-are-owed")
    public BigDecimal getYouAreOwed() {
        return analyticsService.getYouAreOwed();
    }

    @GetMapping("/spending-by-category")
    public List<AnalyticsService.CategorySpendingResponse> getSpendingByCategory() {
        return analyticsService.getSpendingByCategory();
    }

    @GetMapping("/open-groups")
    public List<AnalyticsService.OpenGroupResponse> getOpenGroups() {
        return analyticsService.getOpenGroups();
    }

    @GetMapping("/recent-activity")
    public List<AnalyticsService.RecentActivityResponse> getRecentActivity() {
        return analyticsService.getRecentActivity();
    }
}