package com.kan.expensetracker.budget.service;

import com.kan.expensetracker.budget.domain.Budget;
import com.kan.expensetracker.budget.repo.BudgetRepository;
import com.kan.expensetracker.category.domain.Category;
import com.kan.expensetracker.common.api.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BudgetRepositoryFacade {

    private final BudgetRepository budgetRepository;

    public Budget upsert(String budgetMonth, Category category, BigDecimal amount, BigDecimal warn, BigDecimal exceed) {
        var existing = (category == null)
                ? budgetRepository.findByBudgetMonthAndCategoryIsNull(budgetMonth)
                : budgetRepository.findByBudgetMonthAndCategory_Id(budgetMonth, category.getId());

        Budget b = existing.orElseGet(Budget::new);
        b.setBudgetMonth(budgetMonth);
        b.setCategory(category);
        b.setAmount(amount);
        b.setThresholdWarn(warn);
        b.setThresholdExceed(exceed);

        try {
            return budgetRepository.save(b);
        } catch (DataIntegrityViolationException e) {
            // In case of race conditions on unique constraint
            throw new IllegalStateException("Budget already exists for this month/category");
        }
    }

    public List<Budget> listForMonth(String budgetMonth) {
        return budgetRepository.findAllByBudgetMonth(budgetMonth);
    }

    public void delete(long id) {
        Budget b = budgetRepository.findById(id).orElseThrow(() -> new NotFoundException("Budget not found: " + id));
        budgetRepository.delete(b);
    }
}
