package com.ai.sdlc.expensetracker.insights.api;

import com.ai.sdlc.expensetracker.insights.api.dto.MonthlyDashboardResponse;
import com.ai.sdlc.expensetracker.insights.service.InsightsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/insights")
@RequiredArgsConstructor
public class InsightsController {

    private final InsightsService insightsService;

    // User Story 3 + 5: Monthly dashboard with category breakdown + budgets/alerts
    @GetMapping("/monthly")
    public MonthlyDashboardResponse monthly(@RequestParam YearMonth month) {
        return insightsService.monthlyDashboard(month);
    }
}
