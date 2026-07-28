package com.ai.sdlc.expensetracker.budget.service;

import com.ai.sdlc.expensetracker.budget.api.dto.BudgetResponse;
import com.ai.sdlc.expensetracker.budget.api.dto.BudgetUpsertRequest;
import com.ai.sdlc.expensetracker.budget.domain.Budget;
import com.ai.sdlc.expensetracker.budget.domain.CategoryBudget;
import com.ai.sdlc.expensetracker.budget.repo.BudgetRepository;
import com.ai.sdlc.expensetracker.common.api.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;

    @Transactional
    public BudgetResponse upsert(BudgetUpsertRequest req) {
        Budget budget = budgetRepository.findByMonth(req.month())
                .orElseGet(() -> Budget.builder().budgetMonth(req.month()).build());

        budget.setMonthlyBudget(req.monthlyBudget());

        List<CategoryBudget> categoryBudgets = Optional.ofNullable(req.categoryBudgets()).orElse(List.of()).stream()
                .map(cb -> CategoryBudget.builder()
                        .category(cb.category().trim())
                        .budgetAmount(cb.budgetAmount())
                        .build())
                .toList();

        budget.replaceCategoryBudgets(categoryBudgets);
        Budget saved = budgetRepository.save(budget);
        return toResponse(saved);
    }

    public BudgetResponse getByMonth(java.time.YearMonth month) {
        Budget budget = budgetRepository.findByMonth(month)
                .orElseThrow(() -> new NotFoundException("Budget not found for month: " + month));
        return toResponse(budget);
    }

    private BudgetResponse toResponse(Budget b) {
        return new BudgetResponse(
                b.getId(),
                b.getBudgetMonth(),
                b.getMonthlyBudget(),
                b.getCategoryBudgets().stream()
                        .map(cb -> new BudgetResponse.CategoryBudgetLine(cb.getCategory(), cb.getBudgetAmount()))
                        .toList()
        );
    }
}
