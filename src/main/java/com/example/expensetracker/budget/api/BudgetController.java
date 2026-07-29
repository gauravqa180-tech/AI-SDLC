package com.example.expensetracker.budget.api;

import com.example.expensetracker.budget.api.dto.BudgetProgressResponse;
import com.example.expensetracker.budget.api.dto.BudgetResponse;
import com.example.expensetracker.budget.api.dto.BudgetUpsertRequest;
import com.example.expensetracker.budget.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    // US5: create/update
    @PostMapping
    public BudgetResponse upsert(@Valid @RequestBody BudgetUpsertRequest request) {
        return budgetService.upsert(request);
    }

    // US5: delete
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        budgetService.delete(id);
    }

    // US5: list budgets for month
    @GetMapping
    public List<BudgetResponse> list(@RequestParam int year, @RequestParam int month) {
        return budgetService.list(year, month);
    }

    // US5: progress for a category
    @GetMapping("/progress")
    public BudgetProgressResponse progress(@RequestParam int year,
                                          @RequestParam int month,
                                          @RequestParam String category) {
        return budgetService.progress(year, month, category);
    }
}
