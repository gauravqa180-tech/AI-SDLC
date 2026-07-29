package com.ai.sdlc.expensetracker.api;

import com.ai.sdlc.expensetracker.api.dto.*;
import com.ai.sdlc.expensetracker.service.ExpenseService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    /**
     * KAN-138: create expense (baseline)
     */
    @PostMapping
    public ExpenseResponse create(@Valid @RequestBody ExpenseCreateRequest req) {
        return expenseService.create(req);
    }

    /**
     * KAN-138: edit expense
     */
    @PutMapping("/{id}")
    public ExpenseResponse update(@PathVariable long id, @Valid @RequestBody ExpenseUpdateRequest req) {
        return expenseService.update(id, req);
    }

    /**
     * KAN-139: search/filter/sort + pagination
     */
    @GetMapping
    public Page<ExpenseResponse> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false, defaultValue = "date_desc") String sort,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size
    ) {
        return expenseService.list(q, category, from, to, minAmount, maxAmount, sort, page, size);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable long id) {
        expenseService.delete(id);
    }

    /**
     * Baseline: monthly total
     */
    @GetMapping("/monthly-total")
    public BigDecimal monthlyTotal(@RequestParam String month) {
        return expenseService.monthlyTotal(YearMonth.parse(month));
    }

    /**
     * KAN-140: insights
     */
    @GetMapping("/insights")
    public InsightsResponse insights(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("to must be on/after from");
        }
        return expenseService.insights(from, to);
    }

    /**
     * KAN-141: export CSV (respects filters)
     */
    @GetMapping(value = "/export", produces = "text/csv")
    public void exportCsv(
            HttpServletResponse response,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false, defaultValue = "date_desc") String sort
    ) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=expenses.csv");
        response.setContentType(MediaType.TEXT_PLAIN_VALUE);

        try (PrintWriter writer = response.getWriter()) {
            expenseService.writeCsv(writer, q, category, from, to, minAmount, maxAmount, sort);
        }
    }
}
