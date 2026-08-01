package com.example.expensetracker.api;

import com.example.expensetracker.api.dto.ExpenseRequest;
import com.example.expensetracker.api.dto.ExpenseResponse;
import com.example.expensetracker.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping
    public List<ExpenseResponse> list() {
        return expenseService.listAll();
    }

    @GetMapping("/{id}")
    public ExpenseResponse get(@PathVariable Long id) {
        return expenseService.getById(id);
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> create(@Valid @RequestBody ExpenseRequest request) {
        ExpenseResponse created = expenseService.create(request);
        return ResponseEntity.created(URI.create("/api/expenses/" + created.id())).body(created);
    }

    /**
     * User Story 1: edit/update expense.
     * Requires request.version for concurrency control.
     */
    @PutMapping("/{id}")
    public ExpenseResponse update(@PathVariable Long id, @Valid @RequestBody ExpenseRequest request) {
        return expenseService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        expenseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/monthly-total")
    public ExpenseService.MonthlyTotal monthlyTotal(@RequestParam("month") @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return expenseService.monthlyTotal(month);
    }
}
