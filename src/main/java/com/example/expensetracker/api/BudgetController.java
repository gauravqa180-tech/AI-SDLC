package com.example.expensetracker.api;

import com.example.expensetracker.api.dto.BudgetStatusResponse;
import com.example.expensetracker.api.dto.UpsertBudgetRequest;
import com.example.expensetracker.domain.MonthlyBudget;
import com.example.expensetracker.service.BudgetService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    /**
     * US5: Create/update monthly budget for a category.
     */
    @PostMapping
    public MonthlyBudget upsert(@Valid @RequestBody UpsertBudgetRequest req) {
        return budgetService.upsert(req);
    }

    /**
     * US5: Spend vs budget overview for month.
     */
    @GetMapping("/status")
    public List<BudgetStatusResponse> status(
            @RequestParam
            @Pattern(regexp = "\\d{4}-\\d{2}", message = "month must be yyyy-MM")
            String month
    ) {
        return budgetService.status(month);
    }
}
