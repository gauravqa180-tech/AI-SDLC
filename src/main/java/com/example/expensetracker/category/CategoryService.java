package com.example.expensetracker.category;

import com.example.expensetracker.category.dto.CategoryCreateRequest;
import com.example.expensetracker.category.dto.CategoryDeleteRequest;
import com.example.expensetracker.category.dto.CategoryRenameRequest;
import com.example.expensetracker.common.api.NotFoundException;
import com.example.expensetracker.expense.Expense;
import com.example.expensetracker.expense.ExpenseRepository;
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

    public List<Category> listAll() {
        return categoryRepository.findAll();
    }

    public Category create(CategoryCreateRequest req) {
        categoryRepository.findByNameIgnoreCase(req.name())
                .ifPresent(c -> {
                    throw new IllegalArgumentException("Category already exists");
                });

        Category category = Category.builder()
                .name(req.name().trim())
                .active(true)
                .build();
        try {
            return categoryRepository.save(category);
        } catch (DataIntegrityViolationException e) {
            // In case of concurrent request.
            throw new IllegalArgumentException("Category already exists");
        }
    }

    public Category rename(long id, CategoryRenameRequest req) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found: " + id));

        String newName = req.name().trim();
        categoryRepository.findByNameIgnoreCase(newName)
                .filter(existing -> !existing.getId().equals(category.getId()))
                .ifPresent(c -> {
                    throw new IllegalArgumentException("Category already exists");
                });

        category.setName(newName);
        try {
            return categoryRepository.save(category);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Category already exists");
        }
    }

    @Transactional
    public void delete(long id, CategoryDeleteRequest req) {
        if (id == req.reassignToCategoryId()) {
            throw new IllegalArgumentException("Cannot reassign to the same category");
        }

        Category toDelete = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found: " + id));

        Category replacement = categoryRepository.findById(req.reassignToCategoryId())
                .orElseThrow(() -> new NotFoundException("Replacement category not found: " + req.reassignToCategoryId()));

        // Reassign all expenses (including soft deleted) to preserve history.
        List<Expense> impacted = expenseRepository.findAll((root, q, cb) -> cb.equal(root.get("category").get("id"), id));
        for (Expense e : impacted) {
            e.setCategory(replacement);
        }
        expenseRepository.saveAll(impacted);

        categoryRepository.delete(toDelete);
    }
}
