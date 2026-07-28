package com.kan.expensetracker.category.service;

import com.kan.expensetracker.category.api.dto.CategoryResponse;
import com.kan.expensetracker.category.domain.Category;

public final class CategoryMapper {
    private CategoryMapper() {}

    public static CategoryResponse toResponse(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.isActive());
    }
}
