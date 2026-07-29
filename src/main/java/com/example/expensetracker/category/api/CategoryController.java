package com.example.expensetracker.category.api;

import com.example.expensetracker.category.CategoryService;
import com.example.expensetracker.category.api.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryResponse> list() {
        return categoryService.list();
    }

    @PostMapping
    public CategoryResponse create(@Valid @RequestBody CategoryRequest request) {
        return categoryService.create(request);
    }

    @PutMapping("/{id}")
    public CategoryResponse rename(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return categoryService.rename(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, @RequestParam(required = false) Long reassignToCategoryId) {
        categoryService.delete(id, reassignToCategoryId);
    }

    @PostMapping("/merge")
    public void merge(@Valid @RequestBody CategoryMergeRequest request) {
        categoryService.merge(request);
    }
}
