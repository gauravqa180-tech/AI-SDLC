package com.example.expensetracker.category;

import com.example.expensetracker.common.api.NotFoundException;
import com.example.expensetracker.category.api.dto.*;
import com.example.expensetracker.expense.Expense;
import com.example.expensetracker.expense.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> list() {
        return categoryRepository.findAll().stream()
                .sorted(Comparator.comparing(Category::getName, String.CASE_INSENSITIVE_ORDER))
                .map(c -> new CategoryResponse(c.getId(), c.getName()))
                .toList();
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new IllegalArgumentException("Category already exists: " + request.name());
        }
        Category saved = categoryRepository.save(Category.builder().name(request.name().trim()).build());
        return new CategoryResponse(saved.getId(), saved.getName());
    }

    @Transactional
    public CategoryResponse rename(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found: " + id));

        String newName = request.name().trim();
        categoryRepository.findByNameIgnoreCase(newName)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Category name already in use: " + newName);
                });

        category.setName(newName);
        Category saved = categoryRepository.save(category);
        return new CategoryResponse(saved.getId(), saved.getName());
    }

    @Transactional
    public void delete(Long id, Long reassignToCategoryId) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found: " + id));

        Category reassignment = null;
        if (reassignToCategoryId != null) {
            reassignment = categoryRepository.findById(reassignToCategoryId)
                    .orElseThrow(() -> new NotFoundException("Reassign category not found: " + reassignToCategoryId));
        }

        List<Expense> affected = expenseRepository.findAll((root, query, cb) -> cb.equal(root.get("category").get("id"), id));
        for (Expense e : affected) {
            e.setCategory(reassignment);
        }
        expenseRepository.saveAll(affected);

        categoryRepository.delete(category);
    }

    @Transactional
    public void merge(CategoryMergeRequest request) {
        if (request.sourceCategoryId().equals(request.targetCategoryId())) {
            throw new IllegalArgumentException("sourceCategoryId and targetCategoryId must be different");
        }

        Category source = categoryRepository.findById(request.sourceCategoryId())
                .orElseThrow(() -> new NotFoundException("Source category not found: " + request.sourceCategoryId()));
        Category target = categoryRepository.findById(request.targetCategoryId())
                .orElseThrow(() -> new NotFoundException("Target category not found: " + request.targetCategoryId()));

        List<Expense> affected = expenseRepository.findAll((root, query, cb) -> cb.equal(root.get("category").get("id"), source.getId()));
        for (Expense e : affected) {
            e.setCategory(target);
        }
        expenseRepository.saveAll(affected);

        categoryRepository.delete(source);
    }
}
