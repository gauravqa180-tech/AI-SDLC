package com.ai.sdlc.expensetracker.budget.api;

import com.ai.sdlc.expensetracker.budget.api.dto.*;
import com.ai.sdlc.expensetracker.budget.service.BudgetService;
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

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public BudgetResponse upsert(@Valid @RequestBody BudgetUpsertRequest req) {
    return budgetService.upsert(req);
  }

  @GetMapping
  public List<BudgetResponse> list(@RequestParam String month) {
    return budgetService.list(month);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable long id) {
    budgetService.delete(id);
  }

  @GetMapping("/alerts")
  public List<BudgetAlertResponse> alerts(@RequestParam String month) {
    return budgetService.alerts(month);
  }
}
