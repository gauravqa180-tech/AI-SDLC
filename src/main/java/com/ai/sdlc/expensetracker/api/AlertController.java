package com.ai.sdlc.expensetracker.api;

import com.ai.sdlc.expensetracker.api.dto.AlertResponse;
import com.ai.sdlc.expensetracker.service.BudgetAlertService;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final BudgetAlertService budgetAlertService;

    /**
     * Convenience endpoint to compute alerts for a month on-demand.
     * In-app alert generation also occurs on expense create/update.
     */
    @PostMapping("/evaluate")
    public List<AlertResponse> evaluate(@RequestParam @Pattern(regexp = "^\\d{4}-\\d{2}$") String month) {
        YearMonth ym = YearMonth.parse(month);
        return budgetAlertService.evaluateAndCreateAlerts(ym.atEndOfMonth());
    }

    @PostMapping("/evaluate-date")
    public List<AlertResponse> evaluateDate(@RequestParam LocalDate date) {
        return budgetAlertService.evaluateAndCreateAlerts(date);
    }
}
