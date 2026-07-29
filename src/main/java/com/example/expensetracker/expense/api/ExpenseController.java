package com.example.expensetracker.expense.api;

import com.example.expensetracker.expense.ExpenseService;
import com.example.expensetracker.expense.api.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    // User stories: create + validation
    @PostMapping
    public ExpenseResponse create(@Valid @RequestBody ExpenseUpsertRequest request) {
        return expenseService.create(request);
    }

    // User story: edit expense
    @PutMapping("/{id}")
    public ExpenseResponse update(@PathVariable Long id, @Valid @RequestBody ExpenseUpsertRequest request) {
        return expenseService.update(id, request);
    }

    // Existing v1: delete expense
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        expenseService.delete(id);
    }

    // User story: list with search/filter/sort
    @GetMapping
    public List<ExpenseResponse> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) String q,
            @RequestParam(required = false, defaultValue = "expenseDate") String sortBy,
            @RequestParam(required = false, defaultValue = "DESC") Sort.Direction direction
    ) {
        return expenseService.list(from, to, categoryId, minAmount, maxAmount, q, sortBy, direction);
    }

    // Existing v1: monthly total
    @GetMapping("/monthly-total")
    public MonthlyTotalResponse monthlyTotal(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return expenseService.monthlyTotal(month);
    }

    // User story: monthly category breakdown
    @GetMapping("/monthly-breakdown")
    public MonthlyCategoryBreakdownResponse monthlyBreakdown(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return expenseService.monthlyBreakdown(month);
    }
}
