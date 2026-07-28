package com.aisdlc.expensetracker.csv;

import com.aisdlc.expensetracker.csv.dto.CsvImportResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/csv/expenses")
@RequiredArgsConstructor
public class CsvController {

    private final CsvService csvService;

    @GetMapping("/export")
    public ResponseEntity<ByteArrayResource> export() {
        byte[] bytes = csvService.exportExpensesCsv();
        ByteArrayResource res = new ByteArrayResource(bytes);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=expenses.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(bytes.length)
                .body(res);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CsvImportResponse importCsv(@RequestPart("file") MultipartFile file) {
        return csvService.importExpensesCsv(file);
    }
}
