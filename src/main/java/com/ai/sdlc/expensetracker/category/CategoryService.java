package com.ai.sdlc.expensetracker.category;

import com.ai.sdlc.expensetracker.common.NotFoundException;
import com.ai.sdlc.expensetracker.category.dto.CategoryCreateRequest;
import com.ai.sdlc.expensetracker.category.dto.CategoryResponse;
import com.ai.sdlc.expensetracker.category.dto.CategoryUpdateRequest;
import com.ai.sdlc.expensetracker.expense.ExpenseRepository;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;

    public CategoryService(CategoryRepository categoryRepository, ExpenseRepository expenseRepository) {
        this.categoryRepository = categoryRepository;
        this.expenseRepository = expenseRepository;
    }

    public List<CategoryResponse> list(boolean includeInactive) {
        return categoryRepository.findAll().stream()
                .filter(c -> includeInactive || c.isActive())
                .map(CategoryService::toResponse)
                .toList();
    }

    @Transactional
    public CategoryResponse create(CategoryCreateRequest request) {
        Category c = new Category();
        c.setName(request.name().trim());
        c.setNormalizedName(normalize(request.name()));
        c.setActive(true);

        try {
            return toResponse(categoryRepository.save(c));
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("category already exists");
        }
    }

    @Transactional
    public CategoryResponse rename(long id, CategoryUpdateRequest request) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("category not found: " + id));
        c.setName(request.name().trim());
        c.setNormalizedName(normalize(request.name()));

        try {
            return toResponse(categoryRepository.save(c));
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("category already exists");
        }
    }

    @Transactional
    public void disable(long id) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("category not found: " + id));
        c.setActive(false);
        categoryRepository.save(c);
    }

    /**
     * Merge source category into target category:
     * - reassign all expenses to target
     * - deactivate source
     */
    @Transactional
    public void merge(long sourceCategoryId, long targetCategoryId) {
        if (sourceCategoryId == targetCategoryId) {
            throw new IllegalArgumentException("sourceCategoryId and targetCategoryId must be different");
        }
        Category source = categoryRepository.findById(sourceCategoryId)
                .orElseThrow(() -> new NotFoundException("category not found: " + sourceCategoryId));
        Category target = categoryRepository.findById(targetCategoryId)
                .orElseThrow(() -> new NotFoundException("category not found: " + targetCategoryId));

        // bulk update via JPA: simplest approach is to load IDs by query. For v1 scale, iterate.
        expenseRepository.findAll().stream()
                .filter(e -> e.getCategory().getId().equals(sourceCategoryId))
                .forEach(e -> e.setCategory(target));

        source.setActive(false);
        categoryRepository.save(source);
    }

    private static CategoryResponse toResponse(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.isActive(), c.getCreatedAt(), c.getUpdatedAt());
    }

    private static String normalize(String name) {
        return name == null ? null : name.trim().toLowerCase();
    }
}
