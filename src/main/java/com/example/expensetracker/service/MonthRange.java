package com.example.expensetracker.service;

import java.time.LocalDate;
import java.time.YearMonth;

public record MonthRange(YearMonth month, LocalDate start, LocalDate end) {

    public static MonthRange of(YearMonth month) {
        return new MonthRange(month, month.atDay(1), month.atEndOfMonth());
    }
}
