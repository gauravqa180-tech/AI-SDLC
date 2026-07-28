package com.kan.expensetracker.insights.api;

import com.kan.expensetracker.insights.service.InsightsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/insights")
@RequiredArgsConstructor
public class InsightsController {

    private final InsightsService insightsService;

    // US3: monthly totals by category
    @GetMapping("/monthly-by-category")
    public Map<String, BigDecimal> monthlyByCategory(@RequestParam int year, @RequestParam int month) {
        return insightsService.monthlyByCategory(year, month);
    }
}
