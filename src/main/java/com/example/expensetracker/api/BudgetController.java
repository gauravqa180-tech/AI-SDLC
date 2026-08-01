package com.example.expensetracker.api;

import com.example.expensetracker.api.dto.*;
import com.example.expensetracker.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    // Story 5: create/update overall or per-category budget (upsert)
    @PutMapping
    public BudgetResponse upsert(@Valid @RequestBody BudgetUpsertRequest request) {
        return budgetService.upsert(request);
    }

    @GetMapping
    public List<BudgetResponse> listByMonth(@RequestParam String month) {
        return budgetService.listByMonth(month);
    }

    @GetMapping("/progress")
    public BudgetProgressResponse progress(@RequestParam String month) {
        return budgetService.progress(month);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        budgetService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
