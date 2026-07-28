package com.ai.sdlc.expensetracker.expense.api.dto;

import java.util.List;

public record ImportResultResponse(
        int totalRows,
        int importedRows,
        int failedRows,
        List<RowError> errors
) {
    public record RowError(int rowNumber, String message) {
    }
}
