package com.aisdlc.expensetracker.expense;

import com.aisdlc.expensetracker.expense.dto.ExpenseQuery;
import com.aisdlc.expensetracker.expense.dto.ExpenseResponse;
import com.aisdlc.expensetracker.expense.dto.ExpenseUpsertRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse create(@Valid @RequestBody ExpenseUpsertRequest req) {
        return expenseService.create(req);
    }

    @PutMapping("/{id}")
    public ExpenseResponse update(@PathVariable Long id, @Valid @RequestBody ExpenseUpsertRequest req) {
        return expenseService.update(id, req);
    }

    @GetMapping
    public List<ExpenseResponse> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) java.time.LocalDate startDate,
            @RequestParam(required = false) java.time.LocalDate endDate,
            @RequestParam(required = false) java.math.BigDecimal minAmount,
            @RequestParam(required = false) java.math.BigDecimal maxAmount,
            @RequestParam(required = false) String sort
    ) {
        return expenseService.list(new ExpenseQuery(q, categoryId, startDate, endDate, minAmount, maxAmount, sort));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        expenseService.delete(id);
    }
}
