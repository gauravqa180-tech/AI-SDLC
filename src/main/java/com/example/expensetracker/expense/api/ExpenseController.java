package com.example.expensetracker.expense.api;

import com.example.expensetracker.expense.api.dto.ExpenseCreateRequest;
import com.example.expensetracker.expense.api.dto.ExpenseResponse;
import com.example.expensetracker.expense.api.dto.ExpenseUpdateRequest;
import com.example.expensetracker.expense.service.ExpenseService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
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
    public ExpenseResponse create(@Valid @RequestBody ExpenseCreateRequest request) {
        return expenseService.create(request);
    }

    @PutMapping("/{id}")
    public ExpenseResponse update(@PathVariable Long id, @Valid @RequestBody ExpenseUpdateRequest request) {
        return expenseService.update(id, request);
    }

    @GetMapping
    public List<ExpenseResponse> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        String sortProperty = switch (sortBy) {
            case "amount" -> "amount";
            case "date" -> "expenseDate";
            default -> "expenseDate";
        };

        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return expenseService.list(startDate, endDate, category, minAmount, maxAmount, keyword, Sort.by(direction, sortProperty));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        expenseService.delete(id);
    }

    @GetMapping("/monthly-total")
    public BigDecimal monthlyTotal(
            @RequestParam
            @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "month must be in yyyy-MM format")
            String month
    ) {
        YearMonth ym = YearMonth.parse(month);
        return expenseService.monthlyTotal(ym);
    }
}
