package com.aisdlc.expensetracker.expense.api;

import com.aisdlc.expensetracker.expense.api.dto.ExpenseCreateRequest;
import com.aisdlc.expensetracker.expense.api.dto.ExpenseResponse;
import com.aisdlc.expensetracker.expense.api.dto.ExpenseUpdateRequest;
import com.aisdlc.expensetracker.expense.api.dto.MonthlyTotalResponse;
import com.aisdlc.expensetracker.expense.service.ExpenseService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse create(@Valid @RequestBody ExpenseCreateRequest request) {
        return expenseService.create(request);
    }

    @GetMapping
    public List<ExpenseResponse> list() {
        return expenseService.list();
    }

    @PutMapping("/{id}")
    public ExpenseResponse update(@PathVariable long id, @Valid @RequestBody ExpenseUpdateRequest request) {
        return expenseService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        expenseService.delete(id);
    }

    @GetMapping("/monthly-total")
    public MonthlyTotalResponse monthlyTotal(@RequestParam int year, @RequestParam int month) {
        return expenseService.monthlyTotal(year, month);
    }
}
