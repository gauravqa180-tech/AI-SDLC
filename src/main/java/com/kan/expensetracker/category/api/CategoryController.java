package com.kan.expensetracker.category.api;

import com.kan.expensetracker.category.api.dto.CategoryRequest;
import com.kan.expensetracker.category.api.dto.CategoryResponse;
import com.kan.expensetracker.category.service.CategoryMapper;
import com.kan.expensetracker.category.service.CategoryService;
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
    public List<CategoryResponse> list(@RequestParam(name = "includeInactive", defaultValue = "false") boolean includeInactive) {
        return categoryService.list(includeInactive).stream().map(CategoryMapper::toResponse).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@Valid @RequestBody CategoryRequest request) {
        return CategoryMapper.toResponse(categoryService.create(request));
    }

    @PutMapping("/{id}")
    public CategoryResponse rename(@PathVariable long id, @Valid @RequestBody CategoryRequest request) {
        return CategoryMapper.toResponse(categoryService.rename(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        categoryService.delete(id);
    }

    @PostMapping("/merge")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void merge(@RequestParam long sourceCategoryId, @RequestParam long targetCategoryId) {
        categoryService.merge(sourceCategoryId, targetCategoryId);
    }
}
