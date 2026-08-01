package com.example.expensetracker.api;

import com.example.expensetracker.api.dto.ExpenseResponse;

import java.util.List;

/**
 * Simple server-side CSV generator.
 *
 * Includes basic CSV injection mitigation: if a cell starts with one of (=,+,-,@), prefix with a single quote.
 */
final class CsvUtil {

    private CsvUtil() {
    }

    static String expensesToCsv(List<ExpenseResponse> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("amount,date,category,note\n");
        for (ExpenseResponse r : rows) {
            sb.append(escapeCell(r.amount() == null ? "" : r.amount().toPlainString())).append(',');
            sb.append(escapeCell(r.date() == null ? "" : r.date().toString())).append(',');
            sb.append(escapeCell(r.category() == null ? "" : r.category())).append(',');
            sb.append(escapeCell(r.note() == null ? "" : r.note())).append('\n');
        }
        return sb.toString();
    }

    private static String escapeCell(String value) {
        String v = value;
        if (!v.isEmpty()) {
            char c = v.charAt(0);
            if (c == '=' || c == '+' || c == '-' || c == '@') {
                v = "'" + v;
            }
        }

        boolean needsQuotes = v.contains(",") || v.contains("\n") || v.contains("\r") || v.contains("\"");
        if (v.contains("\"")) {
            v = v.replace("\"", "\"\"");
        }
        return needsQuotes ? ("\"" + v + "\"") : v;
    }
}
