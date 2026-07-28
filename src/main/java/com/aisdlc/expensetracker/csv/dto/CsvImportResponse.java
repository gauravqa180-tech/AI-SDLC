package com.aisdlc.expensetracker.csv.dto;

import java.util.List;

public record CsvImportResponse(
        int totalRows,
        int importedRows,
        int skippedRows,
        List<RowError> errors
) {
    public record RowError(int rowNumber, String reason) {}
}
