package com.aisdlc.expensetracker.budget;

import com.aisdlc.expensetracker.budget.dto.BudgetProgressResponse;
import com.aisdlc.expensetracker.budget.dto.BudgetResponse;
import com.aisdlc.expensetracker.budget.dto.BudgetUpsertRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @PutMapping("/overall")
    public BudgetResponse upsertOverall(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
            @Valid @RequestBody BudgetUpsertRequest req
    ) {
        return budgetService.upsertOverall(month, req);
    }

    @PutMapping("/categories/{categoryId}")
    public BudgetResponse upsertCategory(
            @PathVariable Long categoryId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
            @Valid @RequestBody BudgetUpsertRequest req
    ) {
        return budgetService.upsertCategory(month, categoryId, req);
    }

    @GetMapping("/progress")
    public BudgetProgressResponse progress(@RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return budgetService.progress(month);
    }

    @PostMapping("/alerts/{alertId}/ack")
    public void acknowledge(@PathVariable Long alertId) {
        budgetService.acknowledgeAlert(alertId);
    }
}
