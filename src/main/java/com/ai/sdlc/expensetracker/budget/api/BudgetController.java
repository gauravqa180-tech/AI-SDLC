package com.ai.sdlc.expensetracker.budget.api;

import com.ai.sdlc.expensetracker.budget.api.dto.BudgetResponse;
import com.ai.sdlc.expensetracker.budget.api.dto.BudgetUpsertRequest;
import com.ai.sdlc.expensetracker.budget.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    // User Story 5: Set/update monthly and category budgets
    @PutMapping
    public BudgetResponse upsert(@Valid @RequestBody BudgetUpsertRequest req) {
        return budgetService.upsert(req);
    }

    @GetMapping
    public BudgetResponse getByMonth(@RequestParam YearMonth month) {
        return budgetService.getByMonth(month);
    }
}
