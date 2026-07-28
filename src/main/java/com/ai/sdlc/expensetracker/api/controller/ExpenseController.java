package com.ai.sdlc.expensetracker.api.controller;

import com.ai.sdlc.expensetracker.api.dto.ExpenseRequest;
import com.ai.sdlc.expensetracker.api.dto.ExpenseResponse;
import com.ai.sdlc.expensetracker.api.mapper.ExpenseMapper;
import com.ai.sdlc.expensetracker.domain.Expense;
import com.ai.sdlc.expensetracker.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    // KAN-73
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse create(@Valid @RequestBody ExpenseRequest request) {
        return ExpenseMapper.toResponse(expenseService.create(request));
    }

    @GetMapping("/{id}")
    public ExpenseResponse get(@PathVariable long id) {
        return ExpenseMapper.toResponse(expenseService.get(id));
    }

    // KAN-75
    @GetMapping
    public Page<ExpenseResponse> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false, name = "q") String q,
            @RequestParam(required = false, name = "sort") List<String> sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<Expense> result = expenseService.list(from, to, category, minAmount, maxAmount, q, sort, page, size);
        return result.map(ExpenseMapper::toResponse);
    }

    // KAN-73
    @PutMapping("/{id}")
    public ExpenseResponse update(@PathVariable long id, @Valid @RequestBody ExpenseRequest request) {
        return ExpenseMapper.toResponse(expenseService.update(id, request));
    }

    // KAN-74
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        expenseService.softDelete(id);
    }

    // KAN-74
    @PostMapping("/{id}/undo-delete")
    public ExpenseResponse undoDelete(@PathVariable long id) {
        return ExpenseMapper.toResponse(expenseService.undoDelete(id));
    }
}
