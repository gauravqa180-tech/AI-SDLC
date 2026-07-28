package com.ai.sdlc.expensetracker.summary.api;

import com.ai.sdlc.expensetracker.summary.api.dto.MonthlyCategoryTotalResponse;
import com.ai.sdlc.expensetracker.summary.service.MonthlySummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/summary")
@RequiredArgsConstructor
public class MonthlySummaryController {

    private final MonthlySummaryService monthlySummaryService;

    // KAN-34
    @GetMapping("/monthly-category-totals")
    public MonthlyCategoryTotalResponse monthlyCategoryTotals(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        return monthlySummaryService.categoryTotals(month);
    }
}
