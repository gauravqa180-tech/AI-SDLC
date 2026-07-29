package com.example.expensetracker.expense.api;

import com.example.expensetracker.expense.api.dto.*;
import com.example.expensetracker.expense.domain.Expense;
import com.example.expensetracker.expense.service.ExpenseService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    // US1: Create
    @PostMapping
    public ExpenseResponse create(@Valid @RequestBody ExpenseRequest request) {
        return toResponse(expenseService.create(request));
    }

    // US1: Edit (PUT)
    @PutMapping("/{id}")
    public ExpenseResponse update(@PathVariable Long id, @Valid @RequestBody ExpenseRequest request) {
        return toResponse(expenseService.update(id, request));
    }

    // US2: List with filter/sort/search/pagination
    @GetMapping
    public Page<ExpenseResponse> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Page<Expense> result = expenseService.search(startDate, endDate, category, q, page, size, sortBy, sortDir);
        return result.map(this::toResponse);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        expenseService.delete(id);
    }

    // Existing feature: monthly total
    @GetMapping("/monthly-total")
    public MonthlyTotalResponse monthlyTotal(
            @RequestParam @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "month must be in YYYY-MM format") String month
    ) {
        YearMonth ym = YearMonth.parse(month);
        BigDecimal total = expenseService.monthlyTotal(ym);
        return new MonthlyTotalResponse(month, total);
    }

    // US3: monthly category breakdown
    @GetMapping("/monthly-breakdown")
    public MonthlyBreakdownResponse monthlyBreakdown(
            @RequestParam @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "month must be in YYYY-MM format") String month
    ) {
        YearMonth ym = YearMonth.parse(month);
        BigDecimal total = expenseService.monthlyTotal(ym);
        List<CategoryTotalResponse> byCategory = expenseService.monthlyCategoryTotals(ym).stream()
                .map(r -> new CategoryTotalResponse(r.getCategory(), r.getTotal()))
                .toList();
        return new MonthlyBreakdownResponse(month, total, byCategory);
    }

    // US4: CSV export
    @GetMapping(value = "/export", produces = "text/csv")
    public void exportCsv(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletResponse response
    ) throws IOException {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("startDate and endDate are required");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate must be on or after startDate");
        }

        String filename = "expenses_%s_to_%s.csv".formatted(startDate, endDate);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
        response.setContentType(MediaType.valueOf("text/csv").toString());

        List<Expense> expenses = expenseService.findBetween(startDate, endDate);

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader("id", "amount", "date", "category", "note")
                .build();

        try (CSVPrinter printer = new CSVPrinter(response.getWriter(), format)) {
            for (Expense e : expenses) {
                printer.printRecord(e.getId(), e.getAmount(), e.getDate(), e.getCategory(), e.getNote());
            }
        }
    }

    private ExpenseResponse toResponse(Expense e) {
        return new ExpenseResponse(e.getId(), e.getAmount(), e.getDate(), e.getCategory(), e.getNote());
    }
}
