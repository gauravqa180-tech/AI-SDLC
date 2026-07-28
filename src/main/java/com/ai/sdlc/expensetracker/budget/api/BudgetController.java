package com.ai.sdlc.expensetracker.budget.api;

import com.ai.sdlc.expensetracker.budget.api.dto.BudgetProgressResponse;
import com.ai.sdlc.expensetracker.budget.api.dto.BudgetResponse;
import com.ai.sdlc.expensetracker.budget.api.dto.BudgetUpsertRequest;
import com.ai.sdlc.expensetracker.budget.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    // KAN-36 create/update
    @PutMapping
    public BudgetResponse upsert(@Valid @RequestBody BudgetUpsertRequest request) {
        return budgetService.upsert(request);
    }

    @GetMapping
    public List<BudgetResponse> listByMonth(@RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return budgetService.listByMonth(month);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        budgetService.delete(id);
    }

    @GetMapping("/progress")
    public BudgetProgressResponse progress(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
            @RequestParam String category
    ) {
        return budgetService.progress(month, category);
    }
}
