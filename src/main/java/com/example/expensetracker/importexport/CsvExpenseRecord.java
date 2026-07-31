package com.example.expensetracker.importexport;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CsvExpenseRecord(LocalDate date, BigDecimal amount, String category, String note) {
}
