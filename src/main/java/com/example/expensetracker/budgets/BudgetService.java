package com.example.expensetracker.budgets;

import com.example.expensetracker.budgets.dto.BudgetStatusDto;
import com.example.expensetracker.budgets.dto.BudgetsOverviewDto;
import com.example.expensetracker.categories.Category;
import com.example.expensetracker.categories.CategoryRepository;
import com.example.expensetracker.expenses.ExpenseRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final OverallMonthlyBudgetRepository overallRepo;
    private final CategoryMonthlyBudgetRepository categoryRepo;
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;

    @Value("${app.budgets.warning-threshold:0.9}")
    private double warningThreshold;

    @Transactional
    public void upsertOverall(YearMonth month, BigDecimal amount) {
        OverallMonthlyBudget b = overallRepo.findByBudgetMonth(month).orElseGet(OverallMonthlyBudget::new);
        b.setBudgetMonth(month);
        b.setAmount(amount);
        overallRepo.save(b);
    }

    @Transactional
    public void clearOverall(YearMonth month) {
        overallRepo.deleteByBudgetMonth(month);
    }

    @Transactional
    public void upsertCategory(YearMonth month, long categoryId, BigDecimal amount) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found: " + categoryId));
        CategoryMonthlyBudget b = categoryRepo.findByBudgetMonthAndCategoryId(month, categoryId)
                .orElseGet(CategoryMonthlyBudget::new);
        b.setBudgetMonth(month);
        b.setCategory(category);
        b.setAmount(amount);
        categoryRepo.save(b);
    }

    @Transactional
    public void clearCategory(YearMonth month, long categoryId) {
        categoryRepo.deleteByBudgetMonthAndCategoryId(month, categoryId);
    }

    @Transactional(readOnly = true)
    public BudgetsOverviewDto overview(YearMonth month) {
        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();

        BigDecimal overallSpent = expenseRepository.sumBetween(from, to);
        BudgetStatusDto overallStatus = overallRepo.findByBudgetMonth(month)
                .map(b -> status(b.getAmount(), overallSpent))
                .orElse(null);

        List<BudgetsOverviewDto.CategoryBudgetOverviewDto> categoryBudgets = new ArrayList<>();
        for (CategoryMonthlyBudget b : categoryRepo.findAllByBudgetMonth(month)) {
            BigDecimal spent = expenseRepository.sumBetweenForCategory(b.getCategory().getId(), from, to);
            categoryBudgets.add(new BudgetsOverviewDto.CategoryBudgetOverviewDto(
                    b.getCategory().getId(),
                    b.getCategory().getName(),
                    status(b.getAmount(), spent)
            ));
        }

        return new BudgetsOverviewDto(month, overallStatus, categoryBudgets);
    }

    @Transactional(readOnly = true)
    public List<BudgetAlert> evaluateAlerts(YearMonth month) {
        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();

        List<BudgetAlert> alerts = new ArrayList<>();

        overallRepo.findByBudgetMonth(month).ifPresent(b -> {
            BigDecimal spent = expenseRepository.sumBetween(from, to);
            BudgetStatusDto st = status(b.getAmount(), spent);
            if (st.warning() || st.exceeded()) {
                alerts.add(new BudgetAlert("OVERALL", null, null, st));
            }
        });

        for (CategoryMonthlyBudget b : categoryRepo.findAllByBudgetMonth(month)) {
            BigDecimal spent = expenseRepository.sumBetweenForCategory(b.getCategory().getId(), from, to);
            BudgetStatusDto st = status(b.getAmount(), spent);
            if (st.warning() || st.exceeded()) {
                alerts.add(new BudgetAlert("CATEGORY", b.getCategory().getId(), b.getCategory().getName(), st));
            }
        }

        return alerts;
    }

    private BudgetStatusDto status(BigDecimal budgetAmount, BigDecimal spentAmount) {
        if (budgetAmount == null) return null;
        BigDecimal remaining = budgetAmount.subtract(spentAmount);
        boolean exceeded = spentAmount.compareTo(budgetAmount) > 0;
        boolean warning = !exceeded && spentAmount.compareTo(budgetAmount.multiply(BigDecimal.valueOf(warningThreshold))) >= 0;
        return new BudgetStatusDto(budgetAmount, spentAmount, remaining, warning, exceeded);
    }

    public record BudgetAlert(String type, Long categoryId, String categoryName, BudgetStatusDto status) {}
}
