package com.ai.sdlc.expensetracker.api;

import com.ai.sdlc.expensetracker.api.dto.*;
import com.ai.sdlc.expensetracker.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    // User Story 1 + 2: create + edit with validation
    @PostMapping("/expenses")
    public ResponseEntity<ExpenseResponse> create(@Valid @RequestBody ExpenseCreateRequest request) {
        return ResponseEntity.ok(expenseService.create(request));
    }

    @PutMapping("/expenses/{id}")
    public ResponseEntity<ExpenseResponse> update(@PathVariable Long id, @Valid @RequestBody ExpenseUpdateRequest request) {
        return ResponseEntity.ok(expenseService.update(id, request));
    }

    // User Story 3: filter/sort/search
    @GetMapping("/expenses")
    public ResponseEntity<List<ExpenseResponse>> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, name = "q") String q,
            @RequestParam(required = false) String sort
    ) {
        Sort parsedSort = SortParser.parse(sort);
        return ResponseEntity.ok(expenseService.list(from, to, category, q, parsedSort));
    }

    @DeleteMapping("/expenses/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        expenseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // User Story 4: monthly category breakdown
    @GetMapping("/insights/monthly/category-breakdown")
    public ResponseEntity<MonthlyCategoryBreakdownResponse> monthlyCategoryBreakdown(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        return ResponseEntity.ok(expenseService.monthlyCategoryBreakdown(month));
    }

    // User Story 5: CSV export
    @GetMapping(value = "/expenses/export.csv", produces = "text/csv")
    public ResponseEntity<String> exportCsv(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, name = "q") String q,
            @RequestParam(required = false) String sort
    ) {
        Sort parsedSort = SortParser.parse(sort);
        List<ExpenseResponse> expenses = expenseService.list(from, to, category, q, parsedSort);

        StringBuilder sb = new StringBuilder();
        sb.append("id,date,category,amount,note\n");
        for (ExpenseResponse e : expenses) {
            sb.append(csv(e.id())).append(',')
                    .append(csv(e.date())).append(',')
                    .append(csv(e.category())).append(',')
                    .append(csv(e.amount())).append(',')
                    .append(csv(e.note()))
                    .append('\n');
        }

        String filename = String.format("expenses_%s_to_%s.csv", from, to);
        String disposition = "attachment; filename=\"" + URLEncoder.encode(filename, StandardCharsets.UTF_8) + "\"";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                .contentType(new MediaType("text", "csv"))
                .body(sb.toString());
    }

    private static String csv(Object value) {
        if (value == null) return "";
        String s = String.valueOf(value);
        boolean needsQuotes = s.contains(",") || s.contains("\n") || s.contains("\r") || s.contains("\"");
        if (!needsQuotes) return s;
        return '"' + s.replace("\"", "\"\"") + '"';
    }
}
