package com.example.expensetracker.api;

import com.example.expensetracker.api.dto.*;
import com.example.expensetracker.domain.Expense;
import com.example.expensetracker.domain.ExpenseCategory;
import com.example.expensetracker.service.ExpenseService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    /**
     * Create expense (baseline capability).
     */
    @PostMapping
    public ExpenseResponse create(@Valid @RequestBody CreateExpenseRequest req) {
        return ExpenseResponse.from(expenseService.create(req));
    }

    /**
     * US1: Edit expense + validation.
     */
    @PutMapping("/{id}")
    public ExpenseResponse update(@PathVariable Long id, @Valid @RequestBody UpdateExpenseRequest req) {
        return ExpenseResponse.from(expenseService.update(id, req));
    }

    /**
     * US2: Search / filter / sort.
     */
    @GetMapping
    public List<ExpenseResponse> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) ExpenseCategory category,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = buildSort(sortBy, sortDir);
        return expenseService.search(startDate, endDate, category, q, sort)
                .stream()
                .map(ExpenseResponse::from)
                .toList();
    }

    /**
     * Baseline: delete.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        expenseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Baseline: monthly total (kept for compatibility).
     */
    @GetMapping("/monthly-total")
    public MonthlyTotalResponse monthlyTotal(
            @RequestParam(required = false)
            @Pattern(regexp = "\\d{4}-\\d{2}", message = "month must be yyyy-MM")
            String month
    ) {
        YearMonth ym = month == null ? YearMonth.now() : YearMonth.parse(month);
        return new MonthlyTotalResponse(ym.toString(), expenseService.monthlyTotal(ym));
    }

    /**
     * US3: Monthly insights (total + by-category breakdown).
     */
    @GetMapping("/monthly-insights")
    public MonthlyInsightsResponse monthlyInsights(
            @RequestParam(required = false)
            @Pattern(regexp = "\\d{4}-\\d{2}", message = "month must be yyyy-MM")
            String month
    ) {
        YearMonth ym = month == null ? YearMonth.now() : YearMonth.parse(month);

        var total = expenseService.monthlyTotal(ym);
        var byCategory = expenseService.monthlyTotalsByCategory(ym)
                .stream()
                .map(arr -> new CategoryTotalResponse((ExpenseCategory) arr[0], (java.math.BigDecimal) arr[1]))
                .toList();

        return new MonthlyInsightsResponse(ym.toString(), total, byCategory);
    }

    /**
     * US4: CSV export of expenses (by date range + optional category/q, using same search).
     * If no start/end passed, defaults to current month.
     */
    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) ExpenseCategory category,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        if (startDate == null && endDate == null) {
            YearMonth now = YearMonth.now();
            startDate = now.atDay(1);
            endDate = now.atEndOfMonth();
        }

        Sort sort = buildSort(sortBy, sortDir);
        List<Expense> expenses = expenseService.search(startDate, endDate, category, q, sort);

        StringBuilder sb = new StringBuilder();
        sb.append("amount,date,category,note\n");
        for (Expense e : expenses) {
            sb.append(csv(e.getAmount() == null ? "" : e.getAmount().toPlainString())).append(',')
                    .append(csv(e.getDate() == null ? "" : e.getDate().toString())).append(',')
                    .append(csv(e.getCategory() == null ? "" : e.getCategory().name())).append(',')
                    .append(csv(e.getNote() == null ? "" : e.getNote()))
                    .append('\n');
        }

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        String filename = "expenses_" + startDate + "_to_" + endDate + ".csv";

        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(bytes);
    }

    private static Sort buildSort(String sortBy, String sortDir) {
        String property = switch (sortBy == null ? "date" : sortBy) {
            case "amount" -> "amount";
            case "date" -> "date";
            default -> "date";
        };
        Sort.Direction dir = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(dir, property).and(Sort.by(Sort.Direction.DESC, "id"));
    }

    private static String csv(String value) {
        String v = value == null ? "" : value;
        boolean mustQuote = v.contains(",") || v.contains("\"") || v.contains("\n") || v.contains("\r");
        if (mustQuote) {
            v = v.replace("\"", "\"\"");
            return "\"" + v + "\"";
        }
        return v;
    }
}
