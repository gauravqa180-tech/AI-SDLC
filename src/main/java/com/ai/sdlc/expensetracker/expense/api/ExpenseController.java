package com.ai.sdlc.expensetracker.expense.api;

import com.ai.sdlc.expensetracker.expense.api.dto.*;
import com.ai.sdlc.expensetracker.expense.domain.Expense;
import com.ai.sdlc.expensetracker.expense.service.ExpenseMapper;
import com.ai.sdlc.expensetracker.expense.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    // User Story 1: Create expense
    @PostMapping
    public ResponseEntity<ExpenseResponse> create(@Valid @RequestBody ExpenseCreateRequest req) {
        Expense created = expenseService.create(req);
        return ResponseEntity
                .created(URI.create("/api/expenses/" + created.getId()))
                .body(ExpenseMapper.toResponse(created));
    }

    // User Story 1: Edit expense
    @PutMapping("/{id}")
    public ExpenseResponse update(@PathVariable long id, @Valid @RequestBody ExpenseUpdateRequest req) {
        return ExpenseMapper.toResponse(expenseService.update(id, req));
    }

    @GetMapping("/{id}")
    public ExpenseResponse get(@PathVariable long id) {
        return ExpenseMapper.toResponse(expenseService.get(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        expenseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // User Story 2: Filter/sort/search + pagination
    @GetMapping
    public PageResponse<ExpenseResponse> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, name = "q") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "expenseDate") String sort,
            @RequestParam(defaultValue = "desc") String dir
    ) {
        Sort.Direction direction = "asc".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        if (!sort.equals("expenseDate") && !sort.equals("amount")) {
            throw new IllegalArgumentException("Invalid sort field. Allowed: expenseDate, amount");
        }
        Pageable pageable = PageRequest.of(page, Math.min(size, 200), Sort.by(direction, sort));

        Page<Expense> result = expenseService.list(from, to, category, q, pageable);
        return new PageResponse<>(
                result.getContent().stream().map(ExpenseMapper::toResponse).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    // User Story 4: CSV export (respects filters)
    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<String> exportCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, name = "q") String q
    ) {
        // Export all matching, capped to avoid memory issues in demo implementation
        Page<Expense> page = expenseService.list(from, to, category, q, PageRequest.of(0, 5000, Sort.by(Sort.Direction.DESC, "expenseDate")));

        StringBuilder sb = new StringBuilder();
        sb.append("id,amount,date,category,note\n");
        for (Expense e : page.getContent()) {
            sb.append(e.getId()).append(",")
                    .append(e.getAmount()).append(",")
                    .append(e.getExpenseDate()).append(",")
                    .append(escapeCsv(e.getCategory())).append(",")
                    .append(escapeCsv(e.getNote()))
                    .append("\n");
        }

        String filename = "expenses.csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(sb.toString());
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        String v = value.replace("\"", "\"\"");
        if (v.contains(",") || v.contains("\n") || v.contains("\r") || v.contains("\"") ) {
            return "\"" + v + "\"";
        }
        return v;
    }
}
