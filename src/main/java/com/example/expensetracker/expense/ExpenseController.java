package com.example.expensetracker.expense;

import com.example.expensetracker.expense.dto.ExpenseCreateRequest;
import com.example.expensetracker.expense.dto.ExpenseResponse;
import com.example.expensetracker.expense.dto.ExpenseUpdateRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse create(@Valid @RequestBody ExpenseCreateRequest req) {
        return toResponse(expenseService.create(req));
    }

    /** User story: Edit expense */
    @PutMapping("/{id}")
    public ExpenseResponse update(@PathVariable long id, @Valid @RequestBody ExpenseUpdateRequest req) {
        return toResponse(expenseService.update(id, req));
    }

    /** User story: Soft delete with undo */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        expenseService.softDelete(id);
    }

    /** User story: Undo delete */
    @PostMapping("/{id}/undo-delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void undoDelete(@PathVariable long id) {
        expenseService.undoDelete(id);
    }

    /**
     * User story: Month selector + search/sort/filters
     *
     * If month is provided (yyyy-MM), start/end are derived from it.
     * Otherwise start/end are required.
     */
    @GetMapping
    public List<ExpenseResponse> list(
            @RequestParam(required = false)
            @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "month must be in yyyy-MM format")
            String month,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "expenseDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        LocalDate[] range = resolveRange(month, start, end);
        Sort sort = resolveSort(sortBy, sortDir);

        return expenseService.list(range[0], range[1], categoryId, minAmount, maxAmount, q, sort)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/monthly-total")
    public BigDecimal monthlyTotal(
            @RequestParam
            @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "month must be in yyyy-MM format")
            String month
    ) {
        return expenseService.monthlyTotal(YearMonth.parse(month));
    }

    private LocalDate[] resolveRange(String month, LocalDate start, LocalDate end) {
        if (month != null && !month.isBlank()) {
            YearMonth ym = YearMonth.parse(month);
            return new LocalDate[]{ym.atDay(1), ym.atEndOfMonth()};
        }
        if (start == null || end == null) {
            throw new IllegalArgumentException("Either month or both start and end must be provided");
        }
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("end must be on or after start");
        }
        return new LocalDate[]{start, end};
    }

    private Sort resolveSort(String sortBy, String sortDir) {
        Sort.Direction dir = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        // allow-list supported sorts
        String property = switch (sortBy) {
            case "amount" -> "amount";
            case "category" -> "category.name";
            case "expenseDate" -> "expenseDate";
            case "createdAt" -> "createdAt";
            default -> throw new IllegalArgumentException("Unsupported sortBy: " + sortBy);
        };
        return Sort.by(dir, property);
    }

    private ExpenseResponse toResponse(Expense e) {
        return new ExpenseResponse(
                e.getId(),
                e.getAmount(),
                e.getExpenseDate(),
                new ExpenseResponse.CategorySummary(e.getCategory().getId(), e.getCategory().getName()),
                e.getNote()
        );
    }
}
