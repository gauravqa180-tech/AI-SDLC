package com.example.expensetracker.categories;

import com.example.expensetracker.categories.dto.CategoryCreateRequest;
import com.example.expensetracker.categories.dto.CategoryDto;
import com.example.expensetracker.categories.dto.CategoryUpdateRequest;
import com.example.expensetracker.expenses.ExpenseRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;

    @Transactional(readOnly = true)
    public List<CategoryDto> list() {
        return categoryRepository.findAll().stream().map(CategoryService::toDto).toList();
    }

    @Transactional
    public CategoryDto create(CategoryCreateRequest req) {
        if (categoryRepository.existsByNameIgnoreCase(req.name().trim())) {
            throw new IllegalArgumentException("Category already exists: " + req.name());
        }
        Category c = new Category();
        c.setName(req.name().trim());
        c.setSystemDefined(false);
        return toDto(categoryRepository.save(c));
    }

    @Transactional
    public CategoryDto rename(long id, CategoryUpdateRequest req) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found: " + id));
        if (c.isSystemDefined()) {
            throw new IllegalArgumentException("System category cannot be renamed");
        }
        String newName = req.name().trim();
        categoryRepository.findByNameIgnoreCase(newName)
                .filter(existing -> !existing.getId().equals(c.getId()))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Category name already in use: " + newName);
                });
        c.setName(newName);
        return toDto(categoryRepository.save(c));
    }

    @Transactional
    public void delete(long id, Long reassignToCategoryId) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found: " + id));
        if (c.isSystemDefined()) {
            throw new IllegalArgumentException("System category cannot be deleted");
        }

        long inUse = expenseRepository.count((root, query, cb) -> cb.and(
                cb.isNull(root.get("deletedAt")),
                cb.equal(root.get("category").get("id"), id)
        ));

        if (inUse > 0) {
            if (reassignToCategoryId == null) {
                throw new IllegalArgumentException("Category is in use by " + inUse + " expenses. Provide reassignToCategoryId.");
            }
            if (reassignToCategoryId.equals(id)) {
                throw new IllegalArgumentException("reassignToCategoryId must be different");
            }
            Category target = categoryRepository.findById(reassignToCategoryId)
                    .orElseThrow(() -> new EntityNotFoundException("Reassign category not found: " + reassignToCategoryId));
            expenseRepository.findAll((root, query, cb) -> cb.and(
                            cb.isNull(root.get("deletedAt")),
                            cb.equal(root.get("category").get("id"), id)
                    )).forEach(e -> e.setCategory(target));
        }

        categoryRepository.delete(c);
    }

    @Transactional(readOnly = true)
    public Category requireCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found: " + id));
    }

    static CategoryDto toDto(Category c) {
        return new CategoryDto(c.getId(), c.getName(), c.isSystemDefined());
    }
}
