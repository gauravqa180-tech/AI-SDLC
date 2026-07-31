package com.aisdlc.expensetracker.api;

import com.aisdlc.expensetracker.api.dto.CreateExpenseRequest;
import com.aisdlc.expensetracker.api.dto.ExpenseResponse;
import com.aisdlc.expensetracker.api.dto.UpdateExpenseRequest;
import com.aisdlc.expensetracker.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    // User story baseline: create expense
    @PostMapping
    public ExpenseResponse create(@Valid @RequestBody CreateExpenseRequest request) {
        return expenseService.create(request);
    }

    // User story 2: filtering/sorting/search + pagination
    @GetMapping
    public Page<ExpenseResponse> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "date") String sortField,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortOrder
    ) {
        return expenseService.list(startDate, endDate, category, minAmount, maxAmount, q, page, size, sortField, sortOrder);
    }

    @GetMapping("/{id}")
    public ExpenseResponse get(@PathVariable Long id) {
        return expenseService.get(id);
    }

    // User story 1: edit/update expense
    @PutMapping("/{id}")
    public ExpenseResponse update(@PathVariable Long id, @Valid @RequestBody UpdateExpenseRequest request) {
        return expenseService.update(id, request);
    }

    // User story 1: safer delete (backend supports undo)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        expenseService.delete(id);
    }

    @PostMapping("/{id}/undo-delete")
    public ExpenseResponse undoDelete(@PathVariable Long id) {
        return expenseService.undoDelete(id);
    }

    // Baseline: monthly total
    @GetMapping("/monthly-total")
    public BigDecimal monthlyTotal(@RequestParam String month) {
        // month format: YYYY-MM
        YearMonth ym = YearMonth.parse(month);
        return expenseService.monthlyTotal(ym);
    }
}
