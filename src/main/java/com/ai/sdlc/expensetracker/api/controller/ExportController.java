package com.ai.sdlc.expensetracker.api.controller;

import com.ai.sdlc.expensetracker.domain.Expense;
import com.ai.sdlc.expensetracker.repository.ExpenseSpecifications;
import com.ai.sdlc.expensetracker.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/exports")
@RequiredArgsConstructor
public class ExportController {

    private final ExpenseRepository expenseRepository;

    // KAN-77
    @GetMapping(value = "/expenses.csv", produces = "text/csv")
    public org.springframework.http.ResponseEntity<byte[]> exportCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        Specification<Expense> spec = Specification.where(ExpenseSpecifications.notDeleted())
                .and(ExpenseSpecifications.dateFrom(from))
                .and(ExpenseSpecifications.dateTo(to));

        List<Expense> expenses = expenseRepository.findAll(spec, org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Order.asc("expenseDate"),
                org.springframework.data.domain.Sort.Order.asc("id")
        ));

        StringBuilder sb = new StringBuilder();
        sb.append("id,date,amount,category,note\n");
        for (Expense e : expenses) {
            sb.append(e.getId()).append(',')
                    .append(e.getExpenseDate()).append(',')
                    .append(formatAmount(e.getAmount())).append(',')
                    .append(csv(e.getCategory())).append(',')
                    .append(csv(e.getNote()))
                    .append('\n');
        }

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        String filename = "expenses" + (from != null ? "_from-" + from : "") + (to != null ? "_to-" + to : "") + ".csv";

        return org.springframework.http.ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(bytes);
    }

    private static String csv(String value) {
        if (value == null) {
            return "";
        }
        boolean needsQuoting = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r");
        String escaped = value.replace("\"", "\"\"");
        return needsQuoting ? "\"" + escaped + "\"" : escaped;
    }

    private static String formatAmount(BigDecimal amount) {
        return amount == null ? "0.00" : amount.toPlainString();
    }
}
