package com.ai.sdlc.expensetracker.budget;

import com.ai.sdlc.expensetracker.category.Category;
import com.ai.sdlc.expensetracker.category.CategoryRepository;
import com.ai.sdlc.expensetracker.common.NotFoundException;
import com.ai.sdlc.expensetracker.budget.dto.BudgetResponse;
import com.ai.sdlc.expensetracker.budget.dto.BudgetUpsertRequest;
import com.ai.sdlc.expensetracker.budget.dto.CategoryBudgetRequest;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;

    public BudgetService(BudgetRepository budgetRepository, CategoryRepository categoryRepository) {
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public BudgetResponse upsert(int year, int month, BudgetUpsertRequest request) {
        if (month < 1 || month > 12) throw new IllegalArgumentException("month must be between 1 and 12");

        Budget budget = budgetRepository.findByYearAndMonth(year, month)
                .orElseGet(() -> Budget.builder().year(year).month(month).build());

        budget.setOverallAmount(request.overallAmount());

        // Reset category budgets (simple v1 behavior)
        budget.getCategoryBudgets().clear();

        List<CategoryBudgetRequest> cbs = request.categoryBudgets() == null ? List.of() : request.categoryBudgets();

        Map<Long, Category> categories = categoryRepository.findAllById(
                        cbs.stream().map(CategoryBudgetRequest::categoryId).toList()
                ).stream().collect(Collectors.toMap(Category::getId, Function.identity()));

        for (CategoryBudgetRequest cb : cbs) {
            Category category = categories.get(cb.categoryId());
            if (category == null) {
                throw new NotFoundException("category not found: " + cb.categoryId());
            }
            budget.getCategoryBudgets().add(CategoryBudget.builder()
                    .budget(budget)
                    .category(category)
                    .amount(cb.amount())
                    .build());
        }

        Budget saved = budgetRepository.save(budget);
        return toResponse(saved);
    }

    public Budget findOrNull(int year, int month) {
        return budgetRepository.findByYearAndMonth(year, month).orElse(null);
    }

    private static BudgetResponse toResponse(Budget b) {
        List<BudgetResponse.CategoryBudgetResponse> cbs = b.getCategoryBudgets().stream()
                .map(cb -> new BudgetResponse.CategoryBudgetResponse(
                        cb.getCategory().getId(),
                        cb.getCategory().getName(),
                        cb.getAmount()
                ))
                .toList();
        return new BudgetResponse(b.getId(), b.getYear(), b.getMonth(), b.getOverallAmount(), cbs, b.getCreatedAt(), b.getUpdatedAt());
    }
}
