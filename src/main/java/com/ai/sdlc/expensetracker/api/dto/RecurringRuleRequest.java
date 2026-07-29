package com.ai.sdlc.expensetracker.api.dto;

import com.ai.sdlc.expensetracker.domain.RecurringExpenseRule;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecurringRuleRequest(
        @NotNull @DecimalMin(value = "0.01", inclusive = true) BigDecimal amount,
        @NotBlank @Size(max = 100) String category,
        @Size(max = 1000) String note,
        @NotNull RecurringExpenseRule.Frequency frequency,
        @NotNull LocalDate startDate,
        boolean paused
) {
}
