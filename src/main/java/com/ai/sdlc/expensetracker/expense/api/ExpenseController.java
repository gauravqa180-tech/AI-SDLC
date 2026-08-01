package com.ai.sdlc.expensetracker.expense.api;

import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ai.sdlc.expensetracker.expense.api.dto.DeleteResponse;
import com.ai.sdlc.expensetracker.expense.api.dto.ExpenseCreateRequest;
import com.ai.sdlc.expensetracker.expense.api.dto.ExpenseResponse;
import com.ai.sdlc.expensetracker.expense.api.dto.ExpenseUpdateRequest;
import com.ai.sdlc.expensetracker.expense.api.dto.MonthlySummaryResponse;
import com.ai.sdlc.expensetracker.expense.api.dto.PagedResponse;
import com.ai.sdlc.expensetracker.expense.api.dto.UndoDeleteResponse;
import com.ai.sdlc.expensetracker.expense.service.ExpenseService;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService service;

    public ExpenseController(ExpenseService service) {
        this.service = service;
    }

    // User Story 1: create and edit
    @PostMapping
    public ResponseEntity<ExpenseResponse> create(@Valid @RequestBody ExpenseCreateRequest req) {
        return ResponseEntity.status(201).body(service.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> update(@PathVariable Long id, @Valid @RequestBody ExpenseUpdateRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    // User Story 3: list + search/filter/sort
    @GetMapping
    public ResponseEntity<PagedResponse<ExpenseResponse>> list(
            @RequestParam Optional<String> q,
            @RequestParam Optional<String> category,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        return ResponseEntity.ok(service.list(q, category, startDate, endDate, page, size, sortBy, sortDir));
    }

    // User Story 2: safe delete + undo
    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteResponse> delete(@PathVariable Long id) {
        return ResponseEntity.ok(service.delete(id));
    }

    @PostMapping("/{id}/undo-delete")
    public ResponseEntity<UndoDeleteResponse> undoDelete(@PathVariable Long id) {
        return ResponseEntity.ok(service.undoDelete(id));
    }

    // User Story 4: monthly summary + category breakdown
    @GetMapping("/summary/monthly")
    public ResponseEntity<MonthlySummaryResponse> monthlySummary(
            @RequestParam(required = false) String month
    ) {
        YearMonth ym = (month == null || month.isBlank()) ? YearMonth.now() : YearMonth.parse(month);
        return ResponseEntity.ok(service.monthlySummary(ym));
    }

    // User Story 5: CSV export
    @GetMapping(value = "/export", produces = "text/csv")
    public void export(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> endDate,
            HttpServletResponse response
    ) throws Exception {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/csv");
        String filename = "expenses.csv";
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");

        try (PrintWriter writer = response.getWriter()) {
            service.exportCsv(startDate, endDate, writer);
        }
    }
}
