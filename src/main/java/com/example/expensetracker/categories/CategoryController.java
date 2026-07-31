package com.example.expensetracker.categories;

import com.example.expensetracker.categories.dto.CategoryCreateRequest;
import com.example.expensetracker.categories.dto.CategoryDto;
import com.example.expensetracker.categories.dto.CategoryUpdateRequest;
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
    public List<CategoryDto> list() {
        return categoryService.list();
    }

    @PostMapping
    public CategoryDto create(@Valid @RequestBody CategoryCreateRequest req) {
        return categoryService.create(req);
    }

    @PutMapping("/{id}")
    public CategoryDto rename(@PathVariable long id, @Valid @RequestBody CategoryUpdateRequest req) {
        return categoryService.rename(id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable long id, @RequestParam(required = false) Long reassignToCategoryId) {
        categoryService.delete(id, reassignToCategoryId);
    }
}
