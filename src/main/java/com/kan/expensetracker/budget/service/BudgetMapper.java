package com.kan.expensetracker.budget.service;

import com.kan.expensetracker.budget.api.dto.BudgetResponse;
import com.kan.expensetracker.budget.domain.Budget;

import java.time.YearMonth;

public final class BudgetMapper {
    private BudgetMapper() {}

    public static BudgetResponse toResponse(Budget b) {
        YearMonth ym = YearMonth.parse(b.getBudgetMonth());
        return new BudgetResponse(
                b.getId(),
                ym.getYear(),
                ym.getMonthValue(),
                b.getCategory() == null ? null : b.getCategory().getId(),
                b.getCategory() == null ? null : b.getCategory().getName(),
                b.getAmount(),
                b.getThresholdWarn(),
                b.getThresholdExceed()
        );
    }
}
