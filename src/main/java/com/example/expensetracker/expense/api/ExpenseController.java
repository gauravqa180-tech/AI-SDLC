package com.example.expensetracker.expense.api;

import com.example.expensetracker.expense.api.dto.ExpenseCreateRequest;
import com.example.expensetracker.expense.api.dto.ExpenseResponse;
import com.example.expensetracker.expense.api.dto.ExpenseUpdateRequest;
import com.example.expensetracker.expense.api.dto.ExpenseWithAlertResponse;
import com.example.expensetracker.expense.api.dto.MonthlyTotalResponse;
import com.example.expensetracker.expense.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    // US1 + base CRUD: create (with alert)
    @PostMapping
    public ExpenseWithAlertResponse create(@Valid @RequestBody ExpenseCreateRequest request) {
        return expenseService.createWithAlert(request);
    }

    // US3: list/search/filter/sort
    @GetMapping
    public Page<ExpenseResponse> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal amountMin,
            @RequestParam(required = false) BigDecimal amountMax,
            @RequestParam(required = false, name = "q") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return expenseService.search(from, to, category, amountMin, amountMax, q, pageable);
    }

    @GetMapping("/{id}")
    public ExpenseResponse get(@PathVariable Long id) {
        return expenseService.get(id);
    }

    // US1: edit/update (with alert)
    @PutMapping("/{id}")
    public ExpenseWithAlertResponse update(@PathVariable Long id, @Valid @RequestBody ExpenseUpdateRequest request) {
        return expenseService.updateWithAlert(id, request);
    }

    // US2: soft delete
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        expenseService.softDelete(id);
    }

    // US2: restore
    @PostMapping("/{id}/restore")
    public ExpenseResponse restore(@PathVariable Long id) {
        return expenseService.restore(id);
    }

    // v1 monthly total (used by US1/US2 acceptance criteria too)
    @GetMapping("/monthly-total")
    public MonthlyTotalResponse monthlyTotal(@RequestParam int year, @RequestParam int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        BigDecimal total = expenseService.totalBetween(start, end);
        return new MonthlyTotalResponse(start, end, total);
    }
}