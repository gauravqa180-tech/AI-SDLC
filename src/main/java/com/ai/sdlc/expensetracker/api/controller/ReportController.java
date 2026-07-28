package com.ai.sdlc.expensetracker.api.controller;

import com.ai.sdlc.expensetracker.api.dto.MonthlyBreakdownResponse;
import com.ai.sdlc.expensetracker.api.dto.MonthlyTotalResponse;
import com.ai.sdlc.expensetracker.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // KAN-? (existing) + KAN-76 compatibility
    @GetMapping("/monthly-total")
    public MonthlyTotalResponse monthlyTotal(@RequestParam String month) {
        YearMonth ym = YearMonth.parse(month);
        return new MonthlyTotalResponse(ym.toString(), reportService.monthlyTotal(ym));
    }

    // KAN-76
    @GetMapping("/monthly-breakdown")
    public MonthlyBreakdownResponse monthlyBreakdown(@RequestParam String month) {
        YearMonth ym = YearMonth.parse(month);
        return reportService.monthlyBreakdown(ym);
    }
}
