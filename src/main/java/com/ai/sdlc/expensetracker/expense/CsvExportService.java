package com.ai.sdlc.expensetracker.expense;

import com.ai.sdlc.expensetracker.expense.dto.ExpenseResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class CsvExportService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    public String toCsv(List<ExpenseResponse> expenses) {
        StringBuilder sb = new StringBuilder();
        sb.append("Date,Amount,Category,Note\n");
        for (ExpenseResponse e : expenses) {
            sb.append(DATE.format(e.expenseDate())).append(',');
            sb.append(formatAmount(e.amount())).append(',');
            sb.append(csvEscape(e.categoryName())).append(',');
            sb.append(csvEscape(e.note()));
            sb.append('\n');
        }
        return sb.toString();
    }

    private static String formatAmount(BigDecimal amount) {
        if (amount == null) return "";
        return amount.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
    }

    private static String csvEscape(String s) {
        if (s == null) return "";
        boolean needsQuote = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        String v = s.replace("\"", "\"\"");
        return needsQuote ? '"' + v + '"' : v;
    }

    public void validateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("from and to are required");
        }
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("to must be >= from");
        }
    }
}
