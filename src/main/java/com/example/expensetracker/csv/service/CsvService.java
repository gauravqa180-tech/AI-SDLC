package com.example.expensetracker.csv.service;

import com.example.expensetracker.csv.api.dto.CsvImportResult;
import com.example.expensetracker.expense.api.dto.ExpenseCreateRequest;
import com.example.expensetracker.expense.service.ExpenseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvService {

    private final ExpenseService expenseService;

    public CsvService(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    /**
     * US6 import format (header required): amount,date,category,note
     * date in ISO-8601: yyyy-MM-dd
     */
    @Transactional
    public CsvImportResult importCsv(InputStream is) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String header = br.readLine();
            if (header == null) {
                return new CsvImportResult(0, 0, 0, List.of());
            }

            int rowNumber = 1; // header row
            int total = 0;
            int imported = 0;
            List<CsvImportResult.RowError> errors = new ArrayList<>();

            String line;
            while ((line = br.readLine()) != null) {
                rowNumber++;
                if (line.isBlank()) continue;
                total++;
                try {
                    String[] parts = splitCsvLine(line);
                    if (parts.length < 3) {
                        throw new IllegalArgumentException("Expected at least 3 columns: amount,date,category,(note)");
                    }
                    BigDecimal amount = new BigDecimal(parts[0].trim());
                    LocalDate date = LocalDate.parse(parts[1].trim());
                    String category = parts[2].trim();
                    String note = parts.length >= 4 ? emptyToNull(parts[3]) : null;

                    expenseService.create(new ExpenseCreateRequest(amount, date, category, note));
                    imported++;
                } catch (Exception e) {
                    errors.add(new CsvImportResult.RowError(rowNumber, e.getMessage(), line));
                }
            }

            return new CsvImportResult(total, imported, total - imported, errors);
        }
    }

    private static String emptyToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    /**
     * Minimal CSV split with support for quoted values.
     */
    static String[] splitCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
                continue;
            }
            if (c == ',' && !inQuotes) {
                tokens.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString());
        return tokens.toArray(new String[0]);
    }
}
