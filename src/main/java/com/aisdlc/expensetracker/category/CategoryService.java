package com.aisdlc.expensetracker.category;

import com.aisdlc.expensetracker.category.dto.CategoryCreateRequest;
import com.aisdlc.expensetracker.category.dto.CategoryResponse;
import com.aisdlc.expensetracker.category.dto.CategoryUpdateRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public CategoryResponse create(CategoryCreateRequest req) {
        String normalized = req.name().trim();
        if (categoryRepository.existsByNameIgnoreCase(normalized)) {
            throw new IllegalArgumentException("Category already exists");
        }

        Category saved;
        try {
            saved = categoryRepository.save(Category.builder().name(normalized).build());
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Category already exists");
        }
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> list() {
        return categoryRepository.findAll().stream()
                .sorted(Comparator.comparing(Category::getName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CategoryResponse rename(Long id, CategoryUpdateRequest req) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found: " + id));

        String normalized = req.name().trim();
        categoryRepository.findByNameIgnoreCase(normalized)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Category already exists");
                });

        category.setName(normalized);
        try {
            Category saved = categoryRepository.save(category);
            return toResponse(saved);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Category already exists");
        }
    }

    @Transactional(readOnly = true)
    public Category getEntity(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found: " + id));
    }

    private CategoryResponse toResponse(Category c) {
        return new CategoryResponse(c.getId(), c.getName());
    }
}
