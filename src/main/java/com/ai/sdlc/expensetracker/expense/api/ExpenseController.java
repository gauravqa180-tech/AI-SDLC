package com.ai.sdlc.expensetracker.expense.api;

import com.ai.sdlc.expensetracker.expense.api.dto.ExpenseCreateRequest;
import com.ai.sdlc.expensetracker.expense.api.dto.ExpenseResponse;
import com.ai.sdlc.expensetracker.expense.api.dto.ExpenseUpdateRequest;
import com.ai.sdlc.expensetracker.expense.api.dto.MonthlyTotalResponse;
import com.ai.sdlc.expensetracker.expense.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService service;

    @PostMapping
    public ResponseEntity<ExpenseResponse> create(@Valid @RequestBody ExpenseCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping
    public List<ExpenseResponse> list() {
        return service.listAll();
    }

    @PutMapping("/{id}")
    public ExpenseResponse update(@PathVariable long id, @Valid @RequestBody ExpenseUpdateRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        service.delete(id);
    }

    @GetMapping("/monthly-total")
    public MonthlyTotalResponse monthlyTotal(@RequestParam int year, @RequestParam int month) {
        return service.monthlyTotal(year, month);
    }
}
