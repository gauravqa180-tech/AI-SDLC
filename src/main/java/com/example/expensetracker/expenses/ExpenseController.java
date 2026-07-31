package com.example.expensetracker.expenses;

import com.example.expensetracker.budgets.BudgetService;
import com.example.expensetracker.expenses.dto.ExpenseDto;
import com.example.expensetracker.expenses.dto.ExpenseUpsertRequest;
import com.example.expensetracker.expenses.dto.MonthlySummaryDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ExpenseDto create(@Valid @RequestBody ExpenseUpsertRequest req) {
        return expenseService.create(req);
    }

    @PutMapping("/{id}")
    public ExpenseDto update(@PathVariable long id, @Valid @RequestBody ExpenseUpsertRequest req) {
        return expenseService.update(id, req);
    }

    @GetMapping
    public List<ExpenseDto> list(
            @RequestParam Optional<String> q,
            @RequestParam Optional<Long> categoryId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> to,
            @RequestParam Optional<YearMonth> month,
            @RequestParam Optional<String> sortBy,
            @RequestParam Optional<String> sortDir
    ) {
        return expenseService.list(q, categoryId, from, to, month, sortBy, sortDir);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable long id) {
        expenseService.softDelete(id);
    }

    @PostMapping("/{id}/restore")
    public ExpenseDto restore(@PathVariable long id) {
        return expenseService.restore(id);
    }

    @GetMapping("/summary/monthly")
    public MonthlySummaryDto monthlySummary(@RequestParam YearMonth month) {
        return expenseService.monthlySummary(month);
    }

    @GetMapping("/alerts/budgets")
    public List<BudgetService.BudgetAlert> budgetAlerts(@RequestParam YearMonth month) {
        return expenseService.budgetAlertsForMonth(month);
    }
}
