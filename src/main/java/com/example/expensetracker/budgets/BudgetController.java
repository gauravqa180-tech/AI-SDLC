package com.example.expensetracker.budgets;

import com.example.expensetracker.budgets.dto.BudgetUpsertRequest;
import com.example.expensetracker.budgets.dto.BudgetsOverviewDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    public BudgetsOverviewDto overview(@RequestParam YearMonth month) {
        return budgetService.overview(month);
    }

    @PutMapping("/overall")
    public void upsertOverall(@RequestParam YearMonth month, @Valid @RequestBody BudgetUpsertRequest req) {
        budgetService.upsertOverall(month, req.amount());
    }

    @DeleteMapping("/overall")
    public void clearOverall(@RequestParam YearMonth month) {
        budgetService.clearOverall(month);
    }

    @PutMapping("/category/{categoryId}")
    public void upsertCategory(@RequestParam YearMonth month, @PathVariable long categoryId, @Valid @RequestBody BudgetUpsertRequest req) {
        budgetService.upsertCategory(month, categoryId, req.amount());
    }

    @DeleteMapping("/category/{categoryId}")
    public void clearCategory(@RequestParam YearMonth month, @PathVariable long categoryId) {
        budgetService.clearCategory(month, categoryId);
    }
}
