package com.aisdlc.expensetracker.io.api;

import com.aisdlc.expensetracker.expense.domain.Expense;
import com.aisdlc.expensetracker.expense.repo.ExpenseRepository;
import com.aisdlc.expensetracker.expense.service.ExpenseSpecifications;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

    private final ExpenseRepository expenseRepository;

    @GetMapping(value = "/expenses.csv", produces = "text/csv")
    public void export(
            @RequestParam Optional<@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate> from,
            @RequestParam Optional<@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate> to,
            HttpServletResponse response
    ) throws Exception {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Content-Disposition", "attachment; filename=expenses.csv");

        Specification<Expense> spec = Specification.where(ExpenseSpecifications.isActive());
        if (from.isPresent()) spec = spec.and(ExpenseSpecifications.dateGte(from.get()));
        if (to.isPresent()) spec = spec.and(ExpenseSpecifications.dateLte(to.get()));

        List<Expense> expenses = expenseRepository.findAll(spec);

        try (PrintWriter writer = response.getWriter()) {
            writer.println("amount,date,category,note");
            for (Expense e : expenses) {
                writer.print(e.getAmount());
                writer.print(',');
                writer.print(e.getExpenseDate());
                writer.print(',');
                writer.print(escapeCsv(e.getCategory()));
                writer.print(',');
                writer.println(escapeCsv(e.getNote()));
            }
            writer.flush();
        }
    }

    private String escapeCsv(String v) {
        if (v == null) return "";
        boolean needQuotes = v.contains(",") || v.contains("\"") || v.contains("\n") || v.contains("\r");
        String s = v.replace("\"", "\"\"");
        return needQuotes ? '"' + s + '"' : s;
    }
}
