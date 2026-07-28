package com.ai.sdlc.expensetracker.alert.api;

import com.ai.sdlc.expensetracker.alert.domain.BudgetAlert;
import com.ai.sdlc.expensetracker.alert.service.BudgetAlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class BudgetAlertController {

    private final BudgetAlertService budgetAlertService;

    @GetMapping
    public List<BudgetAlert> list(@RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return budgetAlertService.listAlerts(month);
    }
}
