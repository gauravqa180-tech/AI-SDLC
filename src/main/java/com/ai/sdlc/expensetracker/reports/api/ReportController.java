package com.ai.sdlc.expensetracker.reports.api;

import com.ai.sdlc.expensetracker.reports.api.dto.MonthlyTotalResponse;
import com.ai.sdlc.expensetracker.reports.service.ReportService;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.YearMonth;

@RestController
@RequestMapping("/api/v1/reports")
@Validated
public class ReportController {

    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    @GetMapping("/monthly-total")
    public ResponseEntity<MonthlyTotalResponse> monthlyTotal(
            @RequestParam(required = false)
            @Pattern(regexp = "\\d{4}-\\d{2}", message = "month must be in format YYYY-MM")
            String month
    ) {
        YearMonth ym = (month == null || month.isBlank()) ? YearMonth.now() : YearMonth.parse(month);
        BigDecimal total = service.monthlyTotal(ym);
        return ResponseEntity.ok(new MonthlyTotalResponse(ym.toString(), total));
    }
}
