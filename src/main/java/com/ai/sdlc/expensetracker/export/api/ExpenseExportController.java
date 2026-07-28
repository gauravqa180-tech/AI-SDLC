package com.ai.sdlc.expensetracker.export.api;

import com.ai.sdlc.expensetracker.expense.api.dto.ExpenseSearchRequest;
import com.ai.sdlc.expensetracker.expense.domain.Expense;
import com.ai.sdlc.expensetracker.expense.repo.ExpenseRepository;
import com.ai.sdlc.expensetracker.expense.service.ExpenseSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/exports")
@RequiredArgsConstructor
public class ExpenseExportController {

    private final ExpenseRepository expenseRepository;

    // KAN-35
    @GetMapping(value = "/expenses.csv", produces = "text/csv")
    public ResponseEntity<byte[]> exportExpensesCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) String note
    ) {
        Specification<Expense> spec = ExpenseSpecifications.fromSearch(
                new ExpenseSearchRequest(startDate, endDate, category, minAmount, maxAmount, note, null, null)
        );
        List<Expense> expenses = expenseRepository.findAll(spec, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "date"));

        StringBuilder sb = new StringBuilder();
        sb.append("id,date,amount,category,note\n");
        for (Expense e : expenses) {
            sb.append(csv(e.getId()))
                    .append(',').append(csv(e.getDate()))
                    .append(',').append(csv(e.getAmount()))
                    .append(',').append(csv(e.getCategory()))
                    .append(',').append(csv(e.getNote()))
                    .append('\n');
        }

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        String filename = "expenses.csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(bytes);
    }

    private String csv(Object v) {
        if (v == null) return "";
        String s = String.valueOf(v);
        boolean needsQuotes = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        if (!needsQuotes) return s;
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }
}
