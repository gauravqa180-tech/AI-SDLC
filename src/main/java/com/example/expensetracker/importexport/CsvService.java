package com.example.expensetracker.importexport;

import com.example.expensetracker.categories.Category;
import com.example.expensetracker.categories.CategoryRepository;
import com.example.expensetracker.expenses.Expense;
import com.example.expensetracker.expenses.ExpenseRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CsvService {

    private static final List<String> HEADER = List.of("date", "amount", "category", "note");

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public byte[] exportMonth(YearMonth month) {
        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();

        var expenses = expenseRepository.findAll((root, query, cb) -> cb.and(
                cb.isNull(root.get("deletedAt")),
                cb.between(root.get("expenseDate"), from, to)
        ));

        StringBuilder sb = new StringBuilder();
        sb.append(String.join(",", HEADER)).append("\n");
        for (Expense e : expenses) {
            sb.append(e.getExpenseDate()).append(',')
                    .append(e.getAmount()).append(',')
                    .append(escape(e.getCategory().getName())).append(',')
                    .append(escape(Optional.ofNullable(e.getNote()).orElse("")))
                    .append("\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional
    public ImportResult importCsv(InputStream in) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        String headerLine = br.readLine();
        if (headerLine == null) {
            throw new IllegalArgumentException("CSV is empty");
        }

        List<String> headers = parseCsvLine(headerLine);
        if (!normalize(headers).equals(HEADER)) {
            throw new IllegalArgumentException("Invalid header. Expected: " + String.join(",", HEADER));
        }

        int lineNo = 1;
        int imported = 0;
        List<RowError> errors = new ArrayList<>();
        String line;
        while ((line = br.readLine()) != null) {
            lineNo++;
            if (line.isBlank()) continue;
            try {
                List<String> cols = parseCsvLine(line);
                while (cols.size() < 4) cols.add("");

                LocalDate date = LocalDate.parse(cols.get(0).trim());
                BigDecimal amount = new BigDecimal(cols.get(1).trim());
                String categoryName = cols.get(2).trim();
                String note = cols.get(3);

                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("amount must be > 0");
                }
                if (categoryName.isBlank()) {
                    throw new IllegalArgumentException("category is required");
                }

                Category category = categoryRepository.findByNameIgnoreCase(categoryName)
                        .orElseThrow(() -> new EntityNotFoundException("Unknown category: " + categoryName));

                Expense e = new Expense();
                e.setExpenseDate(date);
                e.setAmount(amount);
                e.setCategory(category);
                e.setNote(note.isBlank() ? null : note);
                expenseRepository.save(e);
                imported++;
            } catch (Exception ex) {
                errors.add(new RowError(lineNo, ex.getMessage()));
            }
        }

        return new ImportResult(imported, errors);
    }

    public MediaType csvMediaType() {
        return new MediaType("text", "csv");
    }

    private List<String> normalize(List<String> headers) {
        return headers.stream().map(h -> h.trim().toLowerCase()).toList();
    }

    private String escape(String v) {
        if (v == null) return "";
        boolean needsQuotes = v.contains(",") || v.contains("\"") || v.contains("\n") || v.contains("\r");
        String escaped = v.replace("\"", "\"\"");
        return needsQuotes ? '"' + escaped + '"' : escaped;
    }

    private List<String> parseCsvLine(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (inQuotes) {
                if (ch == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        cur.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    cur.append(ch);
                }
            } else {
                if (ch == '"') {
                    inQuotes = true;
                } else if (ch == ',') {
                    out.add(cur.toString());
                    cur.setLength(0);
                } else {
                    cur.append(ch);
                }
            }
        }
        out.add(cur.toString());
        return out;
    }

    public record ImportResult(int importedCount, List<RowError> errors) {}
    public record RowError(int line, String error) {}
}
