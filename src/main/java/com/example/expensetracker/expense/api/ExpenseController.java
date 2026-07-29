package com.example.expensetracker.expense.api;

import com.example.expensetracker.expense.api.dto.*;
import com.example.expensetracker.expense.service.ExpenseService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService service;

    // User Story 1: add expense
    @PostMapping
    public ResponseEntity<ExpenseResponse> create(@Valid @RequestBody ExpenseCreateRequest request) {
        return ResponseEntity.ok(service.create(request));
    }

    // User Story 1: list + User Story 2: filter/sort/search
    @GetMapping
    public ResponseEntity<Page<ExpenseResponse>> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String direction
    ) {
        return ResponseEntity.ok(service.search(from, to, category, q, page, size, sortBy, direction));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> get(@PathVariable long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    // User Story 1 enhancement: edit expense
    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> update(@PathVariable long id, @Valid @RequestBody ExpenseUpdateRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    // User Story 1: delete expense
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Existing feature: monthly total
    @GetMapping("/monthly-total")
    public ResponseEntity<BigDecimal> monthlyTotal(
            @RequestParam @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "month must be in YYYY-MM format") String month
    ) {
        return ResponseEntity.ok(service.monthlyTotal(YearMonth.parse(month)));
    }

    // User Story 3: category totals report
    @GetMapping("/report")
    public ResponseEntity<ReportResponse> report(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(service.report(from, to));
    }

    // User Story 4: export CSV
    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("Date,Amount,Category,Note\n");

        // pull all rows for range; capped by size. for v1 assume modest dataset
        service.search(from, to, null, null, 0, 200, "expenseDate", "ASC")
                .forEach(e -> {
                    sb.append(e.date()).append(',');
                    sb.append(e.amount()).append(',');
                    sb.append(escape(e.category())).append(',');
                    sb.append(escape(e.note())).append('\n');
                });

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=expenses_" + from + "_to_" + to + ".csv")
                .body(bytes);
    }

    private String escape(String s) {
        if (s == null) return "";
        String v = s.replace("\"", "\"\"");
        if (v.contains(",") || v.contains("\n") || v.contains("\r")) {
            return '"' + v + '"';
        }
        return v;
    }
}
