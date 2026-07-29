package com.example.expensetracker.csv.api.dto;

import java.util.List;

public record CsvImportResult(
        int totalRows,
        int importedRows,
        int failedRows,
        List<RowError> errors
) {
    public record RowError(int rowNumber, String reason, String rawLine) {
    }
}
