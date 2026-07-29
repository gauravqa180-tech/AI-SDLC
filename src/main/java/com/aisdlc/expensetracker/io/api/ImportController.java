package com.aisdlc.expensetracker.io.api;

import com.aisdlc.expensetracker.expense.api.dto.ExpenseRequest;
import com.aisdlc.expensetracker.expense.service.ExpenseService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
public class ImportController {

    private final ExpenseService expenseService;
    private final Validator validator;

    @PostMapping(value = "/expenses.csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportResult importCsv(@RequestPart("file") MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("file is required");
        }

        List<RowError> errors = new ArrayList<>();
        int imported = 0;
        int rowNum = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String header = br.readLine();
            rowNum++;
            if (header == null) {
                throw new IllegalArgumentException("CSV is empty");
            }
            String[] cols = splitCsv(header);
            if (cols.length < 4 || !"amount".equalsIgnoreCase(cols[0]) || !"date".equalsIgnoreCase(cols[1])
                    || !"category".equalsIgnoreCase(cols[2]) || !"note".equalsIgnoreCase(cols[3])) {
                throw new IllegalArgumentException("CSV header must be: amount,date,category,note");
            }

            String line;
            while ((line = br.readLine()) != null) {
                rowNum++;
                if (line.isBlank()) continue;

                try {
                    String[] parts = splitCsv(line);
                    if (parts.length < 4) {
                        errors.add(new RowError(rowNum, "Row must have 4 columns"));
                        continue;
                    }

                    ExpenseRequest req = new ExpenseRequest(
                            new BigDecimal(parts[0].trim()),
                            LocalDate.parse(parts[1].trim()),
                            parts[2].trim(),
                            parts[3].isBlank() ? null : parts[3].trim()
                    );

                    Set<ConstraintViolation<ExpenseRequest>> violations = validator.validate(req);
                    if (!violations.isEmpty()) {
                        errors.add(new RowError(rowNum, violations.iterator().next().getMessage()));
                        continue;
                    }

                    expenseService.create(req);
                    imported++;
                } catch (Exception ex) {
                    errors.add(new RowError(rowNum, ex.getMessage()));
                }
            }
        }

        return new ImportResult(imported, errors.size(), errors);
    }

    private String[] splitCsv(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        cur.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    cur.append(c);
                }
            } else {
                if (c == ',') {
                    out.add(cur.toString());
                    cur.setLength(0);
                } else if (c == '"') {
                    inQuotes = true;
                } else {
                    cur.append(c);
                }
            }
        }
        out.add(cur.toString());
        return out.toArray(new String[0]);
    }

    public record ImportResult(int imported, int rejected, List<RowError> errors) {
    }

    public record RowError(int row, String message) {
    }
}
