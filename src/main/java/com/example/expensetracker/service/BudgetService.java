package com.example.expensetracker.service;

import com.example.expensetracker.api.dto.BudgetStatusResponse;
import com.example.expensetracker.api.dto.UpsertBudgetRequest;
import com.example.expensetracker.domain.MonthlyBudget;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.repository.MonthlyBudgetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNullElse;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final MonthlyBudgetRepository monthlyBudgetRepository;
    private final ExpenseRepository expenseRepository;

    @Transactional
    public MonthlyBudget upsert(UpsertBudgetRequest req) {
        MonthlyBudget budget = monthlyBudgetRepository
                .findByMonthAndCategory(req.month(), req.category())
                .orElseGet(() -> MonthlyBudget.builder()
                        .month(req.month())
                        .category(req.category())
                        .build());

        budget.setAmount(req.amount());
        return monthlyBudgetRepository.save(budget);
    }

    @Transactional(readOnly = true)
    public List<BudgetStatusResponse> status(String month) {
        YearMonth ym = YearMonth.parse(month);
        MonthRange r = MonthRange.of(ym);

        List<MonthlyBudget> budgets = monthlyBudgetRepository.findAllByMonth(month);
        List<BudgetStatusResponse> out = new ArrayList<>(budgets.size());

        for (MonthlyBudget b : budgets) {
            // spent for category within month (avoid loading expenses list)
            BigDecimal spent = requireNonNullElse(
                    expenseRepository.sumAmountByCategoryBetweenDates(r.start(), r.end(), b.getCategory()),
                    BigDecimal.ZERO
            );

            BigDecimal remaining = b.getAmount().subtract(spent);
            out.add(new BudgetStatusResponse(month, b.getCategory(), b.getAmount(), spent, remaining));
        }

        return out;
    }
}
