package com.aisdlc.expensetracker.expense.api;

import com.aisdlc.expensetracker.expense.api.dto.ExpenseRequest;
import com.aisdlc.expensetracker.expense.api.dto.ExpenseResponse;
import com.aisdlc.expensetracker.expense.api.dto.MonthlyTotalResponse;
import com.aisdlc.expensetracker.expense.domain.Expense;
import com.aisdlc.expensetracker.expense.service.ExpenseService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ExpenseResponse create(@Valid @RequestBody ExpenseRequest request) {
        return toResponse(expenseService.create(request));
    }

    @GetMapping
    public Page<ExpenseResponse> list(
            @RequestParam Optional<@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate> from,
            @RequestParam Optional<@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate> to,
            @RequestParam Optional<String> category,
            @RequestParam Optional<String> q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "expenseDate") String sort,
            @RequestParam(defaultValue = "desc") String dir
    ) {
        Sort.Direction direction = "asc".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, Math.min(size, 200), Sort.by(direction, sort));
        return expenseService.list(from, to, category, q, pageable).map(this::toResponse);
    }

    @PutMapping("/{id}")
    public ExpenseResponse update(@PathVariable long id, @Valid @RequestBody ExpenseRequest request) {
        return toResponse(expenseService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable long id) {
        expenseService.softDelete(id);
    }

    @GetMapping("/monthly-total")
    public MonthlyTotalResponse monthlyTotal(
            @RequestParam
            @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "month must be in format YYYY-MM")
            String month
    ) {
        YearMonth ym = YearMonth.parse(month);
        return new MonthlyTotalResponse(month, expenseService.monthlyTotal(ym));
    }

    private ExpenseResponse toResponse(Expense e) {
        return new ExpenseResponse(e.getId(), e.getAmount(), e.getExpenseDate(), e.getCategory(), e.getNote());
    }
}
