package com.kan.expensetracker.export.service;

import com.kan.expensetracker.expense.domain.Expense;
import com.kan.expensetracker.expense.repo.ExpenseRepository;
import com.kan.expensetracker.expense.service.ExpenseSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CsvExportService {

    private final ExpenseRepository expenseRepository;

    public void writeExpensesCsv(LocalDate from, LocalDate to, PrintWriter writer) {
        writer.println("Date,Amount,Category,Note");

        Specification<Expense> spec = Specification.where(ExpenseSpecifications.notDeleted())
                .and(ExpenseSpecifications.dateFrom(from))
                .and(ExpenseSpecifications.dateTo(to));

        var sort = Sort.by(Sort.Direction.DESC, "expenseDate").and(Sort.by(Sort.Direction.DESC, "id"));
        for (Expense e : expenseRepository.findAll(spec, sort)) {
            writer.print(e.getExpenseDate());
            writer.print(',');
            writer.print(formatAmount(e.getAmount()));
            writer.print(',');
            writer.print(csvEscape(e.getCategory().getName()));
            writer.print(',');
            writer.print(csvEscape(e.getNote()));
            writer.println();
        }
        writer.flush();
    }

    private static String formatAmount(BigDecimal amount) {
        return amount == null ? "" : amount.toPlainString();
    }

    private static String csvEscape(String value) {
        if (value == null) return "";
        boolean mustQuote = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r");
        String escaped = value.replace("\"", "\"\"");
        return mustQuote ? "\"" + escaped + "\"" : escaped;
    }
}
