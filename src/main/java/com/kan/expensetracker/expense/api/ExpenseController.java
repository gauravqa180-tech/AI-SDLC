package com.kan.expensetracker.expense.api;

import com.kan.expensetracker.expense.api.dto.ExpenseRequest;
import com.kan.expensetracker.expense.api.dto.ExpenseResponse;
import com.kan.expensetracker.expense.api.dto.ExpenseSearchRequest;
import com.kan.expensetracker.expense.service.ExpenseMapper;
import com.kan.expensetracker.expense.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
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

    @Value("${app.expenses.undoWindowSeconds:10}")
    private int undoWindowSeconds;

    // US1: add/create
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse create(@Valid @RequestBody ExpenseRequest request) {
        return ExpenseMapper.toResponse(expenseService.create(request));
    }

    // US1: edit/update
    @PutMapping("/{id}")
    public ExpenseResponse update(@PathVariable long id, @Valid @RequestBody ExpenseRequest request) {
        return ExpenseMapper.toResponse(expenseService.update(id, request));
    }

    // US2: list with filters/sort/search
    @GetMapping
    public List<ExpenseResponse> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) String q,
            @RequestParam(required = false, defaultValue = "date") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDir
    ) {
        ExpenseSearchRequest req = new ExpenseSearchRequest(from, to, categoryId, minAmount, maxAmount, q, sortBy, sortDir);
        return expenseService.search(req).stream().map(ExpenseMapper::toResponse).toList();
    }

    // US1: delete (soft delete)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        expenseService.softDelete(id);
    }

    // US1: undo delete
    @PostMapping("/{id}/undo")
    public ExpenseResponse undoDelete(@PathVariable long id) {
        return ExpenseMapper.toResponse(expenseService.undoDelete(id, undoWindowSeconds));
    }

    // Existing: monthly total (kept)
    @GetMapping("/monthly-total")
    public BigDecimal monthlyTotal(@RequestParam int year, @RequestParam int month) {
        return expenseService.monthlyTotal(YearMonth.of(year, month));
    }
}
