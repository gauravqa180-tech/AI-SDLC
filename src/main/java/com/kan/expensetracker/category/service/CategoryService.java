package com.kan.expensetracker.category.service;

import com.kan.expensetracker.category.api.dto.CategoryRequest;
import com.kan.expensetracker.category.domain.Category;
import com.kan.expensetracker.category.repo.CategoryRepository;
import com.kan.expensetracker.common.api.NotFoundException;
import com.kan.expensetracker.expense.repo.ExpenseRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;

    public List<Category> list(boolean includeInactive) {
        if (includeInactive) {
            return categoryRepository.findAll();
        }
        return categoryRepository.findAll().stream().filter(Category::isActive).toList();
    }

    @Transactional
    public Category create(CategoryRequest request) {
        String normalized = CategoryNormalizer.normalize(request.name());
        Category category = Category.builder()
                .name(request.name().trim())
                .normalizedName(normalized)
                .active(true)
                .build();
        try {
            return categoryRepository.save(category);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Category name already exists");
        }
    }

    @Transactional
    public Category rename(long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found: " + id));

        category.setName(request.name().trim());
        category.setNormalizedName(CategoryNormalizer.normalize(request.name()));

        try {
            return categoryRepository.save(category);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Category name already exists");
        }
    }

    @Transactional
    public void delete(long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found: " + id));

        // Rule: allow delete only if no expenses reference it.
        long usageCount = expenseRepository.count((root, query, cb) -> cb.equal(root.get("category"), category));
        if (usageCount > 0) {
            throw new IllegalStateException("Cannot delete category that is used by expenses. Use merge instead.");
        }
        categoryRepository.delete(category);
    }

    @Transactional
    public void merge(long sourceCategoryId, long targetCategoryId) {
        if (sourceCategoryId == targetCategoryId) return;

        Category source = categoryRepository.findById(sourceCategoryId)
                .orElseThrow(() -> new NotFoundException("Source category not found: " + sourceCategoryId));
        Category target = categoryRepository.findById(targetCategoryId)
                .orElseThrow(() -> new NotFoundException("Target category not found: " + targetCategoryId));

        // Reassign expenses (including deleted ones) from source to target
        var expenses = expenseRepository.findAll((root, query, cb) -> cb.equal(root.get("category"), source));
        expenses.forEach(e -> e.setCategory(target));
        expenseRepository.saveAll(expenses);

        // Deactivate source instead of hard delete for safety
        source.setActive(false);
        categoryRepository.save(source);
    }
}
