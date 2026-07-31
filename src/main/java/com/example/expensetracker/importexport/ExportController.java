package com.example.expensetracker.importexport;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

@RestController
@RequiredArgsConstructor
public class ExportController {

    private final CsvService csvService;

    @GetMapping("/api/exports/expenses.csv")
    public ResponseEntity<byte[]> export(@RequestParam YearMonth month) {
        byte[] bytes = csvService.exportMonth(month);
        String filename = "expenses-" + month + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(csvService.csvMediaType())
                .body(bytes);
    }
}
