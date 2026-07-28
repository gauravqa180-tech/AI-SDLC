package com.aisdlc.expensetracker.csv;

import com.aisdlc.expensetracker.category.Category;
import com.aisdlc.expensetracker.category.CategoryRepository;
import com.aisdlc.expensetracker.csv.dto.CsvImportResponse;
import com.aisdlc.expensetracker.expense.Expense;
import com.aisdlc.expensetracker.expense.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CsvService {

    private static final String[] HEADER = new String[]{"date", "amount", "category", "note"};

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public byte[] exportExpensesCsv() {
        List<Expense> expenses = expenseRepository.findAll();

        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            pw.println(String.join(",", HEADER));
            for (Expense e : expenses) {
                pw.print(escape(e.getDate().toString()));
                pw.print(',');
                pw.print(escape(e.getAmount().toPlainString()));
                pw.print(',');
                pw.print(escape(e.getCategory().getName()));
                pw.print(',');
                pw.println(escape(e.getNote() == null ? "" : e.getNote()));
            }
        }
        return sw.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional
    public CsvImportResponse importExpensesCsv(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("CSV file is required");
        }

        int total = 0;
        int imported = 0;
        List<CsvImportResponse.RowError> errors = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = br.readLine();
            if (headerLine == null) {
                throw new IllegalArgumentException("CSV is empty");
            }

            String[] header = splitCsvLine(headerLine);
            if (header.length < 3) {
                throw new IllegalArgumentException("CSV header must contain: date,amount,category,note");
            }

            // Row numbering: 1 = header
            String line;
            int rowNumber = 1;
            Set<String> dedupe = new HashSet<>();

            while ((line = br.readLine()) != null) {
                rowNumber++;
                if (line.isBlank()) continue;
                total++;

                try {
                    String[] parts = splitCsvLine(line);
                    if (parts.length < 3) {
                        throw new IllegalArgumentException("Row must contain at least date, amount, category");
                    }

                    LocalDate date = parseDate(parts[0]);
                    BigDecimal amount = parseAmount(parts[1]);
                    String categoryName = parts[2].trim();
                    String note = parts.length >= 4 ? parts[3].trim() : "";

                    if (categoryName.isBlank()) {
                        throw new IllegalArgumentException("Category is required");
                    }

                    // Simple duplicate rule: same date+amount+category+note in this import file
                    String key = date + "|" + amount.toPlainString() + "|" + categoryName.toLowerCase() + "|" + note;
                    if (!dedupe.add(key)) {
                        throw new IllegalArgumentException("Duplicate row in import file");
                    }

                    Category category = categoryRepository.findByNameIgnoreCase(categoryName)
                            .orElseGet(() -> categoryRepository.save(Category.builder().name(categoryName).build()));

                    expenseRepository.save(Expense.builder()
                            .date(date)
                            .amount(amount)
                            .category(category)
                            .note(note.isBlank() ? null : note)
                            .build());

                    imported++;
                } catch (Exception e) {
                    errors.add(new CsvImportResponse.RowError(rowNumber, e.getMessage()));
                }
            }

        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read CSV");
        }

        return new CsvImportResponse(total, imported, total - imported, errors);
    }

    private static LocalDate parseDate(String s) {
        try {
            return LocalDate.parse(unquote(s).trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date (expected yyyy-MM-dd)");
        }
    }

    private static BigDecimal parseAmount(String s) {
        try {
            BigDecimal v = new BigDecimal(unquote(s).trim());
            if (v.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Amount must be > 0");
            return v;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid amount");
        }
    }

    private static String escape(String value) {
        if (value == null) return "";
        boolean needsQuotes = value.contains(",") || value.contains("\n") || value.contains("\r") || value.contains("\"");
        String v = value.replace("\"", "\"\"");
        return needsQuotes ? "\"" + v + "\"" : v;
    }

    private static String unquote(String s) {
        String v = s;
        if (v.startsWith("\"") && v.endsWith("\"") && v.length() >= 2) {
            v = v.substring(1, v.length() - 1).replace("\"\"", "\"");
        }
        return v;
    }

    // Minimal CSV parsing for 1-line values with quotes support.
    private static String[] splitCsvLine(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                out.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        out.add(sb.toString());
        return out.toArray(new String[0]);
    }
}
