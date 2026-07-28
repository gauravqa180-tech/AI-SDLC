package com.ai.sdlc.expensetracker.budget;

import com.ai.sdlc.expensetracker.budget.dto.BudgetResponse;
import com.ai.sdlc.expensetracker.budget.dto.BudgetUpsertRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PutMapping("/{year}/{month}")
    public BudgetResponse upsert(@PathVariable int year, @PathVariable int month, @Valid @RequestBody BudgetUpsertRequest request) {
        return budgetService.upsert(year, month, request);
    }
}
