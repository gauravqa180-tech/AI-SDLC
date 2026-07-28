package com.ai.sdlc.expensetracker.insights.api;

import com.ai.sdlc.expensetracker.insights.api.dto.CategoryTotalResponse;
import com.ai.sdlc.expensetracker.insights.api.dto.MonthlyTrendPointResponse;
import com.ai.sdlc.expensetracker.insights.service.InsightsService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/v1/insights")
@Validated
public class InsightsController {

    private final InsightsService service;

    public InsightsController(InsightsService service) {
        this.service = service;
    }

    @GetMapping("/category-breakdown")
    public ResponseEntity<List<CategoryTotalResponse>> categoryBreakdown(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        if (from == null || to == null) {
            YearMonth ym = YearMonth.now();
            from = ym.atDay(1);
            to = ym.atEndOfMonth();
        }
        return ResponseEntity.ok(service.categoryBreakdown(from, to));
    }

    @GetMapping("/monthly-trend")
    public ResponseEntity<List<MonthlyTrendPointResponse>> monthlyTrend(
            @RequestParam(defaultValue = "6") @Min(1) @Max(24) int months
    ) {
        return ResponseEntity.ok(service.monthlyTrend(months));
    }
}
