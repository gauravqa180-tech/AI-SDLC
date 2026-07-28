package com.aisdlc.expensetracker.category;

import com.aisdlc.expensetracker.category.dto.CategoryCreateRequest;
import com.aisdlc.expensetracker.category.dto.CategoryResponse;
import com.aisdlc.expensetracker.category.dto.CategoryUpdateRequest;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@Valid @RequestBody CategoryCreateRequest req) {
        return categoryService.create(req);
    }

    @GetMapping
    public List<CategoryResponse> list() {
        return categoryService.list();
    }

    @PutMapping("/{id}")
    public CategoryResponse rename(@PathVariable Long id, @Valid @RequestBody CategoryUpdateRequest req) {
        return categoryService.rename(id, req);
    }
}
