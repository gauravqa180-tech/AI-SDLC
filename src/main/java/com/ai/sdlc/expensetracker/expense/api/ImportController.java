package com.ai.sdlc.expensetracker.expense.api;

import com.ai.sdlc.expensetracker.expense.api.dto.ExpenseCreateRequest;
import com.ai.sdlc.expensetracker.expense.api.dto.ImportResultResponse;
import com.ai.sdlc.expensetracker.expense.service.ExpenseService;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
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
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ImportController {

    private final ExpenseService expenseService;

    private final Validator validator = buildValidator();

    // User Story 4: CSV import
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportResultResponse importCsv(@RequestPart("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        int total = 0;
        int imported = 0;
        List<ImportResultResponse.RowError> errors = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String header = br.readLine();
            if (header == null) {
                throw new IllegalArgumentException("CSV is empty");
            }

            Map<String, Integer> columns = parseHeader(header);
            requireColumn(columns, "amount");
            requireColumn(columns, "date");
            requireColumn(columns, "category");
            // note optional

            String line;
            int rowNum = 1; // header row
            while ((line = br.readLine()) != null) {
                rowNum++;
                if (line.isBlank()) continue;
                total++;
                try {
                    List<String> fields = splitCsvLine(line);
                    String amountStr = getField(fields, columns, "amount");
                    String dateStr = getField(fields, columns, "date");
                    String category = getField(fields, columns, "category");
                    String note = columns.containsKey("note") ? getField(fields, columns, "note") : null;

                    ExpenseCreateRequest req = new ExpenseCreateRequest(
                            new BigDecimal(amountStr.trim()),
                            LocalDate.parse(dateStr.trim()),
                            category,
                            note
                    );

                    var violations = validator.validate(req);
                    if (!violations.isEmpty()) {
                        String msg = violations.iterator().next().getPropertyPath() + ": " + violations.iterator().next().getMessage();
                        errors.add(new ImportResultResponse.RowError(rowNum, msg));
                        continue;
                    }

                    expenseService.create(req);
                    imported++;
                } catch (Exception e) {
                    errors.add(new ImportResultResponse.RowError(rowNum, e.getMessage()));
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to import CSV: " + e.getMessage());
        }

        return new ImportResultResponse(total, imported, total - imported, errors);
    }

    private static Validator buildValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        return factory.getValidator();
    }

    private static Map<String, Integer> parseHeader(String headerLine) {
        List<String> fields = splitCsvLine(headerLine);
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < fields.size(); i++) {
            map.put(fields.get(i).trim().toLowerCase(), i);
        }
        return map;
    }

    private static void requireColumn(Map<String, Integer> cols, String col) {
        if (!cols.containsKey(col)) {
            throw new IllegalArgumentException("Missing required column: " + col);
        }
    }

    private static String getField(List<String> fields, Map<String, Integer> cols, String col) {
        int idx = cols.get(col);
        if (idx >= fields.size()) return "";
        return fields.get(idx);
    }

    // Minimal CSV parser supporting quoted values with commas.
    private static List<String> splitCsvLine(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        sb.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    sb.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    out.add(sb.toString());
                    sb.setLength(0);
                } else {
                    sb.append(c);
                }
            }
        }
        out.add(sb.toString());
        return out;
    }
}
