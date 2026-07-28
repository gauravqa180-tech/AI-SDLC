package com.ai.sdlc.expensetracker.api;

import com.ai.sdlc.expensetracker.api.dto.*;
import com.ai.sdlc.expensetracker.service.ExpenseService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ExpenseResponse create(@Valid @RequestBody ExpenseCreateRequest req) {
        return expenseService.create(req);
    }

    /**
     * User Story 1: Edit expense.
     */
    @PutMapping("/{id}")
    public ExpenseResponse update(@PathVariable long id, @Valid @RequestBody ExpenseUpdateRequest req) {
        return expenseService.update(id, req);
    }

    /**
     * User Story 2: Filter/search/sort expenses.
     */
    @GetMapping
    public List<ExpenseResponse> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false, name = "q") String q,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = Sort.by(parseDirection(sortDir), sortBy);
        return expenseService.list(from, to, category, minAmount, maxAmount, q, sort);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable long id) {
        expenseService.delete(id);
    }

    @GetMapping("/monthly-total")
    public MonthlyTotalResponse monthlyTotal(@RequestParam @Pattern(regexp = "^\\d{4}-\\d{2}$") String month) {
        return expenseService.monthlyTotal(YearMonth.parse(month));
    }

    /**
     * User Story 3: Monthly summary grouped by category.
     */
    @GetMapping("/monthly-summary")
    public MonthlySummaryResponse monthlySummary(@RequestParam @Pattern(regexp = "^\\d{4}-\\d{2}$") String month) {
        return expenseService.monthlySummary(YearMonth.parse(month));
    }

    /**
     * User Story 4: Export to CSV by date range.
     */
    @GetMapping(value = "/export", produces = "text/csv")
    public void exportCsv(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpServletResponse response
    ) throws IOException {
        List<ExpenseResponse> expenses = expenseService.list(from, to, null, null, null, null, Sort.by(Sort.Direction.ASC, "date"));
        if (expenses.isEmpty()) {
            response.setStatus(204);
            return;
        }

        String filename = "expenses_%s_to_%s.csv".formatted(from, to);
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8);

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded);

        try (PrintWriter writer = response.getWriter()) {
            expenseService.writeCsv(expenses, writer);
        }
    }

    private Sort.Direction parseDirection(String dir) {
        return "asc".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;
    }
}
