package com.example.expensetracker.expense.api;

import com.example.expensetracker.expense.api.dto.BudgetRequest;
import com.example.expensetracker.expense.domain.Budget;
import com.example.expensetracker.expense.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping
    public ResponseEntity<Budget> upsert(@Valid @RequestBody BudgetRequest req) {
        return ResponseEntity.ok(budgetService.upsert(req));
    }

    @GetMapping
    public ResponseEntity<List<Budget>> listByMonth(@RequestParam YearMonth month) {
        return ResponseEntity.ok(budgetService.listByMonth(month));
    }

    @GetMapping("/{id}/progress")
    public ResponseEntity<BudgetService.BudgetProgress> progress(@PathVariable Long id) {
        return ResponseEntity.ok(budgetService.progress(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        budgetService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
