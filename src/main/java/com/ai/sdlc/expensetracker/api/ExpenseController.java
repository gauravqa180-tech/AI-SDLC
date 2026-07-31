package com.ai.sdlc.expensetracker.api;

import com.ai.sdlc.expensetracker.api.dto.ExpenseRequest;
import com.ai.sdlc.expensetracker.api.dto.ExpenseResponse;
import com.ai.sdlc.expensetracker.api.dto.MonthlySummaryResponse;
import com.ai.sdlc.expensetracker.domain.Expense;
import com.ai.sdlc.expensetracker.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse create(@Valid @RequestBody ExpenseRequest request) {
        return toResponse(expenseService.create(request));
    }

    @GetMapping
    public List<ExpenseResponse> list() {
        return expenseService.list().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public ExpenseResponse get(@PathVariable Long id) {
        return toResponse(expenseService.get(id));
    }

    /**
     * User Story #1: edit/update expense.
     */
    @PutMapping("/{id}")
    public ExpenseResponse update(@PathVariable Long id, @Valid @RequestBody ExpenseRequest request) {
        return toResponse(expenseService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        expenseService.delete(id);
    }

    @GetMapping("/summary/monthly")
    public MonthlySummaryResponse monthlySummary(@RequestParam int year, @RequestParam int month) {
        return new MonthlySummaryResponse(year, month, expenseService.monthlyTotal(year, month));
    }

    private ExpenseResponse toResponse(Expense expense) {
        return new ExpenseResponse(expense.getId(), expense.getAmount(), expense.getDate(), expense.getCategory(), expense.getNote());
    }
}
