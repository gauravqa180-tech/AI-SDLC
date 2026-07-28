package com.kan.expensetracker.expense.api;

import com.kan.expensetracker.expense.api.dto.ExpenseRequest;
import com.kan.expensetracker.expense.api.dto.ExpenseResponse;
import com.kan.expensetracker.expense.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService service;

    public ExpenseController(ExpenseService service) {
        this.service = service;
    }

    // US1: Add expense
    @PostMapping
    public ExpenseResponse create(@Valid @RequestBody ExpenseRequest req) {
        return service.create(req);
    }

    // US1: List expenses
    // US2: Search/filter/sort
    @GetMapping
    public List<ExpenseResponse> list(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(name = "sort", required = false) String sort
    ) {
        return service.search(q, category, from, to, sort);
    }

    // US1: Get single expense (needed for edit)
    @GetMapping("/{id}")
    public ExpenseResponse get(@PathVariable long id) {
        return service.getById(id);
    }

    // US1: Edit expense
    @PutMapping("/{id}")
    public ExpenseResponse update(@PathVariable long id, @Valid @RequestBody ExpenseRequest req) {
        return service.update(id, req);
    }

    // US1: Delete expense
    @DeleteMapping("/{id}")
    public void delete(@PathVariable long id) {
        service.delete(id);
    }

    // US1: Monthly total
    @GetMapping("/monthly-total")
    public BigDecimal monthlyTotal(@RequestParam("month") String month) {
        // month format: yyyy-MM
        YearMonth ym = YearMonth.parse(month);
        return service.monthlyTotal(ym);
    }
}
