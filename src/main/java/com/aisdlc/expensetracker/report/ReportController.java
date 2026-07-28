package com.aisdlc.expensetracker.report;

import com.aisdlc.expensetracker.report.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/monthly-total")
    public MonthlyTotalResponse monthlyTotal(@RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return reportService.monthlyTotal(month);
    }

    @GetMapping("/monthly-category-breakdown")
    public MonthlyCategoryBreakdownResponse monthlyCategoryBreakdown(@RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return reportService.monthlyCategoryBreakdown(month);
    }

    @GetMapping("/trends")
    public TrendsResponse trends(@RequestParam(defaultValue = "6") int months) {
        return reportService.trends(months);
    }

    @GetMapping("/highlights")
    public HighlightsResponse highlights(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
            @RequestParam(defaultValue = "5") int topCategories,
            @RequestParam(defaultValue = "5") int topExpenses
    ) {
        return reportService.highlights(month, topCategories, topExpenses);
    }
}
