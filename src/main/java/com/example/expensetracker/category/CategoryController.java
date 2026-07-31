package com.example.expensetracker.category;

import com.example.expensetracker.category.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryResponse> list() {
        return categoryService.listAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@Valid @RequestBody CategoryCreateRequest req) {
        return toResponse(categoryService.create(req));
    }

    @PutMapping("/{id}")
    public CategoryResponse rename(@PathVariable long id, @Valid @RequestBody CategoryRenameRequest req) {
        return toResponse(categoryService.rename(id, req));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id, @Valid @RequestBody CategoryDeleteRequest req) {
        categoryService.delete(id, req);
    }

    private CategoryResponse toResponse(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.isActive());
    }
}
