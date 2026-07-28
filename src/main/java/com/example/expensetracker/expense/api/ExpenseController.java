package com.example.expensetracker.expense.api;

import com.example.expensetracker.expense.api.dto.ExpenseCreateRequest;
import com.example.expensetracker.expense.api.dto.ExpenseResponse;
import com.example.expensetracker.expense.api.dto.ExpenseUpdateRequest;
import com.example.expensetracker.expense.api.dto.MonthlyTotalResponse;
import com.example.expensetracker.expense.service.ExpenseService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ExpenseResponse create(@Valid @RequestBody ExpenseCreateRequest request) {
        return expenseService.create(request);
    }

    @GetMapping
    public List<ExpenseResponse> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) @Pattern(regexp = "(?i)asc|desc", message = "sortDir must be 'asc' or 'desc'") String sortDir
    ) {
        // Additional business validation: date range
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "startDate must be on or before endDate"
            );
        }
        return expenseService.list(startDate, endDate, category, sortBy, sortDir);
    }

    @GetMapping("/{id}")
    public ExpenseResponse get(@PathVariable Long id) {
        return expenseService.get(id);
    }

    @PutMapping("/{id}")
    public ExpenseResponse update(@PathVariable Long id, @Valid @RequestBody ExpenseUpdateRequest request) {
        return expenseService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        expenseService.delete(id);
    }

    @GetMapping("/monthly-total")
    public MonthlyTotalResponse monthlyTotal(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return expenseService.monthlyTotal(month);
    }
}
