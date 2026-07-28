package com.ai.sdlc.expensetracker.expenses.api;

import com.ai.sdlc.expensetracker.expenses.api.dto.ExpenseCreateRequest;
import com.ai.sdlc.expensetracker.expenses.api.dto.ExpenseResponse;
import com.ai.sdlc.expensetracker.expenses.api.dto.ExpenseUpdateRequest;
import com.ai.sdlc.expensetracker.expenses.service.ExpenseService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/expenses")
@Validated
public class ExpenseController {

    private final ExpenseService service;

    public ExpenseController(ExpenseService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> create(@Valid @RequestBody ExpenseCreateRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> update(@PathVariable Long id, @Valid @RequestBody ExpenseUpdateRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> list(
            @RequestParam Optional<String> q,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> dateFrom,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> dateTo,
            @RequestParam Optional<String> category,
            @RequestParam Optional<BigDecimal> minAmount,
            @RequestParam Optional<BigDecimal> maxAmount,
            @RequestParam(defaultValue = "date") @Pattern(regexp = "date|amount", message = "sortBy must be one of: date, amount") String sortBy,
            @RequestParam(defaultValue = "desc") @Pattern(regexp = "asc|desc", message = "sortOrder must be one of: asc, desc") String sortOrder
    ) {
        Sort sort = Sort.by(sortOrder.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        return ResponseEntity.ok(service.list(q, dateFrom, dateTo, category, minAmount, maxAmount, sort));
    }

    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<String> exportCsv(
            @RequestParam Optional<String> q,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> dateFrom,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> dateTo,
            @RequestParam Optional<String> category,
            @RequestParam Optional<BigDecimal> minAmount,
            @RequestParam Optional<BigDecimal> maxAmount,
            @RequestParam(defaultValue = "date") @Pattern(regexp = "date|amount", message = "sortBy must be one of: date, amount") String sortBy,
            @RequestParam(defaultValue = "desc") @Pattern(regexp = "asc|desc", message = "sortOrder must be one of: asc, desc") String sortOrder
    ) {
        Sort sort = Sort.by(sortOrder.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        List<ExpenseResponse> rows = service.list(q, dateFrom, dateTo, category, minAmount, maxAmount, sort);
        StringBuilder sb = new StringBuilder();
        sb.append("expenseId,amount,date,category,note\n");
        for (ExpenseResponse r : rows) {
            sb.append(r.id()).append(',')
                    .append(r.amount()).append(',')
                    .append(r.date()).append(',')
                    .append(csvEscape(r.category())).append(',')
                    .append(csvEscape(r.note()))
                    .append('\n');
        }

        String filename = "expenses_" + LocalDate.now() + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(sb.toString());
    }

    private static String csvEscape(String value) {
        if (value == null) return "";
        boolean mustQuote = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r");
        String v = value.replace("\"", "\"\"");
        return mustQuote ? "\"" + v + "\"" : v;
    }
}
