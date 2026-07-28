package com.ai.sdlc.expensetracker.budgets.api;

import com.ai.sdlc.expensetracker.budgets.api.dto.BudgetAlertResponse;
import com.ai.sdlc.expensetracker.budgets.api.dto.BudgetResponse;
import com.ai.sdlc.expensetracker.budgets.api.dto.BudgetUpsertRequest;
import com.ai.sdlc.expensetracker.budgets.service.BudgetService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/v1/budgets")
@Validated
public class BudgetController {

    private final BudgetService service;

    public BudgetController(BudgetService service) {
        this.service = service;
    }

    @PutMapping("/{month}")
    public ResponseEntity<BudgetResponse> upsert(
            @PathVariable @Pattern(regexp = "\\d{4}-\\d{2}", message = "month must be in format YYYY-MM") String month,
            @Valid @RequestBody BudgetUpsertRequest request
    ) {
        return ResponseEntity.ok(service.upsert(YearMonth.parse(month), request));
    }

    @GetMapping("/{month}")
    public ResponseEntity<BudgetResponse> get(
            @PathVariable @Pattern(regexp = "\\d{4}-\\d{2}", message = "month must be in format YYYY-MM") String month
    ) {
        return ResponseEntity.ok(service.get(YearMonth.parse(month)));
    }

    @PostMapping("/{month}/evaluate-alerts")
    public ResponseEntity<List<BudgetAlertResponse>> evaluateAlerts(
            @PathVariable @Pattern(regexp = "\\d{4}-\\d{2}", message = "month must be in format YYYY-MM") String month
    ) {
        return ResponseEntity.ok(service.evaluateAlertsForMonth(YearMonth.parse(month)));
    }
}
