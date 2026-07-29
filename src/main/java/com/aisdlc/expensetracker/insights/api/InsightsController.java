package com.aisdlc.expensetracker.insights.api;

import com.aisdlc.expensetracker.insights.api.dto.MonthlyInsightsResponse;
import com.aisdlc.expensetracker.insights.service.InsightsService;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/insights")
@RequiredArgsConstructor
public class InsightsController {

    private final InsightsService insightsService;

    @GetMapping("/monthly")
    public MonthlyInsightsResponse monthly(
            @RequestParam
            @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "month must be in format YYYY-MM")
            String month
    ) {
        return insightsService.monthly(YearMonth.parse(month));
    }
}
