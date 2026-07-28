package com.ai.sdlc.expensetracker.expense;

import com.ai.sdlc.expensetracker.expense.dto.ExpenseCreateRequest;
import com.ai.sdlc.expensetracker.expense.dto.ExpenseResponse;
import com.ai.sdlc.expensetracker.expense.dto.ExpenseUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final CsvExportService csvExportService;

    public ExpenseController(ExpenseService expenseService, CsvExportService csvExportService) {
        this.expenseService = expenseService;
        this.csvExportService = csvExportService;
    }

    // User story #1: create
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse create(@Valid @RequestBody ExpenseCreateRequest request) {
        return expenseService.create(request);
    }

    // User story #1: edit/update
    @PutMapping("/{id}")
    public ExpenseResponse update(@PathVariable long id, @Valid @RequestBody ExpenseUpdateRequest request) {
        return expenseService.update(id, request);
    }

    @GetMapping("/{id}")
    public ExpenseResponse get(@PathVariable long id) {
        return expenseService.get(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        expenseService.delete(id);
    }

    // User story #2: list with filters/sort/search. Default to current month if no from/to provided.
    @GetMapping
    public List<ExpenseResponse> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "expenseDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        if (from == null && to == null) {
            YearMonth ym = YearMonth.now();
            from = ym.atDay(1);
            to = ym.atEndOfMonth();
        }
        Sort.Direction dir = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(dir, sortBy);

        return expenseService.list(from, to, categoryId, minAmount, maxAmount, q, sort);
    }

    @GetMapping("/monthly-total")
    public BigDecimal monthlyTotal(@RequestParam int year, @RequestParam int month) {
        return expenseService.monthlyTotal(year, month);
    }

    // User story #3: CSV export
    @GetMapping(value = "/export.csv", produces = "text/csv")
    public ResponseEntity<String> exportCsv(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "expenseDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        csvExportService.validateRange(from, to);
        Sort.Direction dir = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(dir, sortBy);

        List<ExpenseResponse> rows = expenseService.list(from, to, categoryId, minAmount, maxAmount, q, sort);
        String csv = csvExportService.toCsv(rows);

        String filename = String.format("expenses_%s_%s.csv", from, to);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }
}
