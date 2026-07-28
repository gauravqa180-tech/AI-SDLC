package com.example.expensetracker.recurring.api.dto;

import com.example.expensetracker.recurring.domain.RecurringTemplate.ScheduleType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record RecurringTemplateRequest(
    @NotNull @DecimalMin("0.01") BigDecimal amount,
    @NotBlank @Size(max = 64) String category,
    @Size(max = 255) String note,
    @NotNull ScheduleType scheduleType,
    @NotNull Integer dayOfMonth,
    @NotNull LocalDate nextRunDate,
    boolean active
) {}
