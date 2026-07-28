package com.kan.expensetracker.budget.service;

import com.kan.expensetracker.budget.api.dto.BudgetRequest;
import com.kan.expensetracker.budget.domain.Budget;
import com.kan.expensetracker.category.domain.Category;
import com.kan.expensetracker.category.repo.CategoryRepository;
import com.kan.expensetracker.common.api.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepositoryFacade budgetRepositoryFacade;
    private final CategoryRepository categoryRepository;

    @Transactional
    public Budget upsert(BudgetRequest request) {
        YearMonth ym = YearMonth.of(request.year(), request.month());
        String month = Budget.toMonthString(ym);

        Category category = null;
        if (request.categoryId() != null) {
            category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new NotFoundException("Category not found: " + request.categoryId()));
        }

        BigDecimal warn = request.warnThreshold() == null ? new BigDecimal("0.80") : request.warnThreshold();
        BigDecimal exceed = request.exceedThreshold() == null ? new BigDecimal("1.00") : request.exceedThreshold();

        return budgetRepositoryFacade.upsert(month, category, request.amount(), warn, exceed);
    }

    public List<Budget> listForMonth(int year, int month) {
        return budgetRepositoryFacade.listForMonth(Budget.toMonthString(YearMonth.of(year, month)));
    }

    @Transactional
    public void delete(long id) {
        budgetRepositoryFacade.delete(id);
    }
}
