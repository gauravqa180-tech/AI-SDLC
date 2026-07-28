package com.example.expensetracker.budget.api;

import com.example.expensetracker.budget.api.dto.BudgetStatusResponse;
import com.example.expensetracker.budget.api.dto.BudgetUpsertRequest;
import com.example.expensetracker.budget.api.dto.CategoryBudgetUpsertRequest;
import com.example.expensetracker.budget.domain.CategoryBudget;
import com.example.expensetracker.budget.domain.MonthlyBudget;
import com.example.expensetracker.budget.service.BudgetService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

  private final BudgetService budgetService;

  @PutMapping("/monthly")
  public MonthlyBudget upsertMonthly(@RequestBody @Valid BudgetUpsertRequest req) {
    return budgetService.upsertMonthly(req);
  }

  @PutMapping("/category")
  public CategoryBudget upsertCategory(@RequestBody @Valid CategoryBudgetUpsertRequest req) {
    return budgetService.upsertCategory(req);
  }

  @GetMapping("/status")
  public BudgetStatusResponse status(@RequestParam String month) {
    return budgetService.status(month);
  }

  @PostMapping("/evaluate-alerts")
  @ResponseStatus(HttpStatus.OK)
  public List<String> evaluateAlerts(@RequestParam String month) {
    return budgetService.evaluateAlerts(month);
  }
}
