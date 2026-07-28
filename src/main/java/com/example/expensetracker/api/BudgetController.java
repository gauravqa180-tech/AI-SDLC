package com.example.expensetracker.api;

import com.example.expensetracker.api.dto.BudgetProgressResponse;
import com.example.expensetracker.api.dto.BudgetUpsertRequest;
import com.example.expensetracker.service.BudgetService;
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

  @PutMapping
  public BudgetProgressResponse upsert(@Valid @RequestBody BudgetUpsertRequest req) {
    return budgetService.upsert(req);
  }

  @GetMapping
  public List<BudgetProgressResponse> progress(@RequestParam String month) {
    return budgetService.progressForMonth(month);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable long id) {
    budgetService.delete(id);
  }
}
