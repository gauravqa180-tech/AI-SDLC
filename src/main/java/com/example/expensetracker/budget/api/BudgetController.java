package com.example.expensetracker.budget.api;

import com.example.expensetracker.budget.api.dto.BudgetRequest;
import com.example.expensetracker.budget.api.dto.BudgetResponse;
import com.example.expensetracker.budget.api.dto.BudgetStatusResponse;
import com.example.expensetracker.budget.domain.Budget;
import com.example.expensetracker.budget.service.BudgetService;
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

    // US5: upsert overall or category budget
    @PutMapping
    public BudgetResponse upsert(@Valid @RequestBody BudgetRequest request) {
        Budget saved = budgetService.upsert(request);
        return new BudgetResponse(saved.getId(), saved.getMonth(), saved.getCategory(), saved.getAmount());
    }

    @GetMapping
    public List<BudgetResponse> list(@RequestParam @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "month must be in YYYY-MM format") String month) {
        return budgetService.getBudgets(month).stream()
                .map(b -> new BudgetResponse(b.getId(), b.getMonth(), b.getCategory(), b.getAmount()))
                .toList();
    }

    @GetMapping("/status")
    public BudgetStatusResponse status(@RequestParam @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "month must be in YYYY-MM format") String month) {
        return budgetService.status(month);
    }
}
