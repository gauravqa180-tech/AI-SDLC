package com.example.expensetracker.importexport;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class ImportController {

    private final CsvService csvService;

    @PostMapping(value = "/api/imports/expenses.csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CsvService.ImportResult importExpenses(@RequestPart("file") MultipartFile file) throws IOException {
        return csvService.importCsv(file.getInputStream());
    }
}
