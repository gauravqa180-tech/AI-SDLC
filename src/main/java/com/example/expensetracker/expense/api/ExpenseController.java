package com.example.expensetracker.expense.api;

import com.example.expensetracker.expense.api.dto.*;
import com.example.expensetracker.expense.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.StringWriter;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    // User story: add expense
    @PostMapping
    public ResponseEntity<ExpenseResponse> create(@Valid @RequestBody ExpenseCreateRequest req) {
        return ResponseEntity.ok(expenseService.create(req));
    }

    // User story: list + filters/sort/search
    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, name = "q") String q,
            @RequestParam(required = false, defaultValue = "date") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDir
    ) {
        return ResponseEntity.ok(expenseService.list(from, to, category, q, sortBy, sortDir));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.get(id));
    }

    // User story: edit expense
    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> update(@PathVariable Long id, @Valid @RequestBody ExpenseUpdateRequest req) {
        return ResponseEntity.ok(expenseService.update(id, req));
    }

    // User story: delete expense
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        expenseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // User story: monthly total
    @GetMapping("/monthly-total")
    public ResponseEntity<MonthlyTotalResponse> monthlyTotal(@RequestParam YearMonth month) {
        return ResponseEntity.ok(expenseService.monthlyTotal(month));
    }

    // User story: monthly category insights
    @GetMapping("/insights/category-breakdown")
    public ResponseEntity<MonthlyCategoryBreakdownResponse> categoryBreakdown(@RequestParam YearMonth month) {
        return ResponseEntity.ok(expenseService.monthlyCategoryBreakdown(month));
    }

    // User story: export CSV (by query/date range using same list API)
    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<String> exportCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, name = "q") String q,
            @RequestParam(required = false, defaultValue = "date") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDir
    ) {
        List<ExpenseResponse> expenses = expenseService.list(from, to, category, q, sortBy, sortDir);

        StringWriter sw = new StringWriter();
        expenseService.writeCsv(expenses, new java.io.PrintWriter(sw));

        String filename = "expenses.csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(new MediaType("text", "csv"))
                .body(sw.toString());
    }
}
