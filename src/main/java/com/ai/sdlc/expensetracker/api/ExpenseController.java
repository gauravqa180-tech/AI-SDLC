package com.ai.sdlc.expensetracker.api;

import com.ai.sdlc.expensetracker.api.dto.*;
import com.ai.sdlc.expensetracker.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.YearMonth;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ExpenseResponse> create(@Valid @RequestBody ExpenseRequest request) {
        ExpenseResponse created = expenseService.create(request);
        return ResponseEntity.created(URI.create("/api/expenses/" + created.id())).body(created);
    }

    // User Story #3 & #4: search/filter/sort
    @GetMapping
    public ResponseEntity<Page<ExpenseResponse>> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "date,desc") String sort
    ) {
        Sort sortObj = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        return ResponseEntity.ok(expenseService.list(q, startDate, endDate, category, minAmount, maxAmount, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.get(id));
    }

    // User Story #1: edit expense
    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> update(@PathVariable Long id, @Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.ok(expenseService.update(id, request));
    }

    // v1 delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        expenseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // User Story #2: delete with undo
    @DeleteMapping("/{id}/with-undo")
    public ResponseEntity<DeleteWithUndoResponse> deleteWithUndo(@PathVariable Long id) {
        String token = expenseService.deleteWithUndo(id);
        return ResponseEntity.ok(new DeleteWithUndoResponse(token, 10));
    }

    @PostMapping("/undo")
    public ResponseEntity<ExpenseResponse> undo(@RequestParam String token) {
        return ResponseEntity.ok(expenseService.undoDelete(token));
    }

    @GetMapping("/monthly-total")
    public ResponseEntity<MonthlyTotalResponse> monthlyTotal(@RequestParam String month) {
        YearMonth ym = YearMonth.parse(month);
        return ResponseEntity.ok(expenseService.monthlyTotal(ym));
    }

    // User Story #5
    @GetMapping("/monthly-breakdown")
    public ResponseEntity<MonthlyCategoryBreakdownResponse> monthlyBreakdown(@RequestParam String month) {
        YearMonth ym = YearMonth.parse(month);
        return ResponseEntity.ok(expenseService.monthlyBreakdown(ym));
    }

    private Sort parseSort(String sort) {
        // format: field,direction e.g. date,desc or amount,asc
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "date");
        }
        String[] parts = sort.split(",");
        String field = parts[0].trim();
        Sort.Direction dir = (parts.length > 1 && parts[1].equalsIgnoreCase("asc")) ? Sort.Direction.ASC : Sort.Direction.DESC;

        // allow-list fields
        if (!field.equals("date") && !field.equals("amount") && !field.equals("category")) {
            field = "date";
        }
        return Sort.by(dir, field);
    }

    public record DeleteWithUndoResponse(String token, int ttlSeconds) {
    }
}
