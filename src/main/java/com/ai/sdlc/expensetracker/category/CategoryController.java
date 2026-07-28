package com.ai.sdlc.expensetracker.category;

import com.ai.sdlc.expensetracker.category.dto.CategoryCreateRequest;
import com.ai.sdlc.expensetracker.category.dto.CategoryResponse;
import com.ai.sdlc.expensetracker.category.dto.CategoryUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<CategoryResponse> list(@RequestParam(defaultValue = "false") boolean includeInactive) {
        return categoryService.list(includeInactive);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@Valid @RequestBody CategoryCreateRequest request) {
        return categoryService.create(request);
    }

    @PutMapping("/{id}")
    public CategoryResponse rename(@PathVariable long id, @Valid @RequestBody CategoryUpdateRequest request) {
        return categoryService.rename(id, request);
    }

    @PatchMapping("/{id}/disable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disable(@PathVariable long id) {
        categoryService.disable(id);
    }

    @PostMapping("/{id}/merge")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void merge(@PathVariable("id") long sourceCategoryId, @RequestParam long targetCategoryId) {
        categoryService.merge(sourceCategoryId, targetCategoryId);
    }
}
