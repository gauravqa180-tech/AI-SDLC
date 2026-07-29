package com.ai.sdlc.expensetracker.api.dto;

import com.ai.sdlc.expensetracker.domain.RecurringExpenseRule;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecurringRuleResponse(
        Long id,
        BigDecimal amount,
        String category,
        String note,
        RecurringExpenseRule.Frequency frequency,
        LocalDate startDate,
        boolean paused,
        LocalDate lastGeneratedDate
) {
}
